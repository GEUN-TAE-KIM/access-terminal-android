package com.gtkim.mobile_access_control.feature.login.ui

import com.gtkim.mobile_access_control.component.auth.domain.model.AuthError
import com.gtkim.mobile_access_control.component.auth.domain.usecase.LoginUseCase
import com.gtkim.mobile_access_control.component.master.domain.usecase.SyncMasterDataUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveNetworkStateUseCase
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.feature.common.ui.BaseViewModel
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import com.gtkim.mobile_access_control.feature.login.mapper.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
internal class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val syncMasterDataUseCase: SyncMasterDataUseCase,
    private val observeNetworkStateUseCase: ObserveNetworkStateUseCase,
) : BaseViewModel<LoginUiState, LoginSideEffect, LoginIntent>() {

    @OptIn(OrbitExperimental::class)
    override val container: Container<LoginUiState, LoginSideEffect> =
        container(LoginUiState()) {
            repeatOnSubscription {
                launch {
                    observeNetworkStateUseCase().collectLatest { online ->
                        reduce { state.copy(isOnline = online) }
                    }
                }
            }
        }

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.IdChanged -> updateId(intent.value)
            is LoginIntent.PasswordChanged -> updatePassword(intent.value)
            is LoginIntent.Submit -> submit()
            is LoginIntent.DialogDismissed -> dismissDialog()
            is LoginIntent.DialogConfirmed -> confirmDialog()
        }
    }

    private fun updateId(value: String) = intent {
        reduce { state.copy(loginId = value, dialog = null) }
    }

    private fun updatePassword(value: String) = intent {
        reduce { state.copy(password = value, dialog = null) }
    }

    /**
     * 로그인 성공 → master sync 완료까지 await → 검문 화면 진입.
     *
     * master 캐시는 검문 가용성의 전제 (오프라인 자체 판정 + zone picker catalog) 이므로 sync 가
     * 끝나기 전에 검문 화면으로 진입시키면 zone picker 가 빈 catalog 로 떠 운영자가 막힌다.
     * 따라서 WorkManager fire-and-forget 이 아니라 본 ViewModel 이 직접 [SyncMasterDataUseCase]
     * 를 await 한다 — sync 가 검문 가용성의 게이팅 조건.
     *
     * sync 실패는 진입을 막지 않는다 — 이전 sync 의 캐시가 남아 있을 수 있고 (재로그인), Bootstrapper
     * 의 periodic + 네트워크 복구 sync 가 후속으로 자가 회복한다. 단 첫 부팅 + sync 실패 케이스는
     * picker 가 빈 채로 뜨는데, 그건 ScanScreen 의 빈 zones UI 가 사용자에게 신호한다.
     */
    private fun submit() = intent {
        // offline race 가드 — UI 가 canSubmit 으로 1차 차단하지만, 다이얼로그/IME Done 등의 경로로
        // 들어오는 사이 네트워크가 끊겼을 가능성을 봉인. 차단 시 NoConnection 다이얼로그로 즉시 안내.
        if (!state.isOnline) {
            reduce {
                state.copy(
                    loading = false,
                    dialog = DialogState.Error(AuthError.NoConnection.toUiError()),
                )
            }
            return@intent
        }

        reduce { state.copy(loading = true, dialog = null) }

        when (val result = loginUseCase(state.loginId, state.password)) {
            is Outcome.Failure -> reduce {
                state.copy(
                    loading = false,
                    dialog = DialogState.Error(result.error.toUiError()),
                )
            }

            is Outcome.Success -> {
                // sync 결과를 도메인 흐름에서 분기하지 않는다 — Success/Failure 모두 진입 허용.
                // Failure 면 stale cache fallback (이전 sync 결과 또는 빈 catalog). 진단 로그가
                // 필요하면 본 모듈에 Timber 의존을 추가하거나 SyncMasterDataUseCase 안에서 남긴다.
                syncMasterDataUseCase()
                reduce { state.copy(loading = false) }
                postSideEffect(LoginSideEffect.NavigateToScan)
            }
        }
    }

    private fun dismissDialog() = intent {
        reduce { state.copy(dialog = null) }
    }

    /**
     * 로그인 화면의 에러 다이얼로그는 모두 None 액션 — 확인만 누르면 다이얼로그를 닫고 사용자가
     * 다시 폼을 제출하면 된다. (Reauthenticate 는 이미 인증 진입점이라 의미 없음.)
     */
    private fun confirmDialog() = intent {
        reduce { state.copy(dialog = null) }
    }
}
