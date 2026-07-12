package com.gtkim.mobile_access_control.navigation

import androidx.lifecycle.viewModelScope
import com.gtkim.mobile_access_control.R
import com.gtkim.mobile_access_control.component.auth.domain.usecase.LogoutUseCase
import com.gtkim.mobile_access_control.component.sync.domain.model.FlushState
import com.gtkim.mobile_access_control.component.sync.domain.usecase.GetPendingCountUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveNetworkStateUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveOfflineFlushStateUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveQueueDeadLetterUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveQueueOverflowUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.SyncNowUseCase
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.feature.common.ui.BaseViewModel
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

/**
 * [AppShell] 의 ViewModel — 단말 전역 상태(큐 카운트 / 네트워크 / 동기화 다이얼로그) + Drawer 의
 * 로그아웃 액션을 보유한다. 화면별 ViewModel(Scan/Register/History/Stats) 은 여기에 같은 책임을
 * 중복으로 두지 않는다.
 *
 * 인증 라우팅은 반응형 ([AccessNavGraph]): logout 호출 → `AuthRepository.authState` 가
 * `LoggedOut` 으로 전이 → outer NavGraph 의 LaunchedEffect 가 LoginRoute 로 popUpTo.
 * 본 ViewModel 은 navigation 을 직접 트리거하지 않는다.
 */
@HiltViewModel
internal class AppShellViewModel @Inject constructor(
    private val getPendingCountUseCase: GetPendingCountUseCase,
    private val observeNetworkStateUseCase: ObserveNetworkStateUseCase,
    private val syncNowUseCase: SyncNowUseCase,
    private val observeOfflineFlushStateUseCase: ObserveOfflineFlushStateUseCase,
    private val observeQueueOverflowUseCase: ObserveQueueOverflowUseCase,
    private val observeQueueDeadLetterUseCase: ObserveQueueDeadLetterUseCase,
    private val logoutUseCase: LogoutUseCase,
) : BaseViewModel<AppShellUiState, AppShellSideEffect, AppShellIntent>() {

    @OptIn(OrbitExperimental::class)
    override val container: Container<AppShellUiState, AppShellSideEffect> =
        container(AppShellUiState()) {
            repeatOnSubscription {
                launch {
                    getPendingCountUseCase().collectLatest { count ->
                        reduce { state.copy(pendingCount = count) }
                    }
                }
                launch {
                    observeNetworkStateUseCase().collectLatest { online ->
                        reduce { state.copy(isOnline = online) }
                    }
                }
                // 큐 overflow (100건 초과) — 어느 화면에 있든 스낵바로 알림.
                launch {
                    observeQueueOverflowUseCase().collect {
                        postSideEffect(AppShellSideEffect.ShowQueueOverflow)
                    }
                }
                // dead-letter (재시도 10회 초과) — 네트워크/서버 상태 점검 신호.
                launch {
                    observeQueueDeadLetterUseCase().collect {
                        postSideEffect(AppShellSideEffect.ShowQueueDeadLetter)
                    }
                }
            }
        }

    override fun onIntent(intent: AppShellIntent) {
        when (intent) {
            is AppShellIntent.RequestSync -> openSyncConfirm()
            is AppShellIntent.ConfirmSync -> runSync()
            is AppShellIntent.DismissDialog -> dismissDialog()
            is AppShellIntent.Logout -> doLogout()
        }
    }

    private fun openSyncConfirm() = intent {
        if (state.dialog != null) return@intent
        // offline 일 땐 즉시 전송이 불가능하니 단순 안내 다이얼로그로 분기. 운영자가 "확인" 을
        // 눌러도 flush 트리거 없이 닫히기만 한다 (runSync 의 isOnline 가드).
        val dialog = if (state.isOnline) {
            DialogState.Confirm(
                title = UiText.Res(R.string.topbar_sync_confirm_title),
                message = UiText.Res(R.string.topbar_sync_confirm_message),
                confirmLabel = UiText.Res(R.string.topbar_sync_confirm_action),
            )
        } else {
            DialogState.Info(
                title = UiText.Res(R.string.topbar_sync_offline_title),
                message = UiText.Res(R.string.topbar_sync_offline_message),
            )
        }
        reduce { state.copy(dialog = dialog) }
    }

    private fun runSync() {
        intent {
            reduce { state.copy(dialog = null) }
            // Info 다이얼로그의 onConfirm 도 동일 intent 로 들어오므로 offline race 방어 가드.
            // 다이얼로그 노출과 confirm 사이에 네트워크 상태가 바뀌어도 안전하게 차단.
            if (!state.isOnline) return@intent
            // offline flush + master sync 묶음. unique work + KEEP — 진행 중이면 noop, 자동 트리거와
            // 동시 enqueue 돼도 안전 (SyncNowUseCase).
            syncNowUseCase()
        }
        observeNextFlushOutcome()
    }

    /**
     * 수동 트리거 직후 시작되는 일회성 collect. 시작 시점의 stale state 를 무시하기 위해
     * drop(1) 로 첫 emission 을 버린 뒤 다음 종료 상태 1개만 잡는다.
     */
    private fun observeNextFlushOutcome() {
        viewModelScope.launch {
            val result = observeOfflineFlushStateUseCase()
                .drop(1)
                .filter { it == FlushState.Succeeded || it == FlushState.Failed }
                .first()
            intent {
                postSideEffect(
                    if (result == FlushState.Succeeded) AppShellSideEffect.ShowFlushSucceeded
                    else AppShellSideEffect.ShowFlushFailed,
                )
            }
        }
    }

    private fun dismissDialog() = intent {
        if (state.dialog != null) reduce { state.copy(dialog = null) }
    }

    private fun doLogout() {
        viewModelScope.launch { logoutUseCase() }
    }
}
