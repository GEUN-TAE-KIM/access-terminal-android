package com.gtkim.mobile_access_control.feature.history.ui

import com.gtkim.mobile_access_control.component.auth.domain.usecase.LogoutUseCase
import com.gtkim.mobile_access_control.component.history.domain.model.LogFilter
import com.gtkim.mobile_access_control.component.history.domain.usecase.GetAccessLogsUseCase
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.feature.common.ui.BaseViewModel
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.feature.history.mapper.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
internal class HistoryViewModel @Inject constructor(
    private val getAccessLogsUseCase: GetAccessLogsUseCase,
    private val logoutUseCase: LogoutUseCase,
    timeProvider: TimeProvider,
) : BaseViewModel<HistoryUiState, Nothing, HistoryIntent>() {

    /**
     * 진행 중인 GET 의 코루틴 Job 추적용. 새 load 시작 전에 이전 작업을 cancel 한다 —
     * 사용자가 화면을 떠난 동안 살아남아 timeout 응답으로 dialog 를 덮어쓰는 stale write 차단.
     */
    private var loadJob: Job? = null

    @OptIn(OrbitExperimental::class)
    override val container: Container<HistoryUiState, Nothing> =
        // initial isRefreshing = true → 첫 composition 부터 fullscreen 로딩 표시.
        // (false 면 reduce 적용 전 빈 LazyColumn 이 잠깐 보였다가 로딩으로 깜빡거림 — 첫 진입 한정 race.)
        container(HistoryUiState(zoneId = timeProvider.zoneId(), isRefreshing = true)) {
            // 재진입마다 현재 필터로 새로 로드. 사용자가 화면을 떠났다 돌아오면
            // 그 사이 추가/삭제된 출입 기록이 자동 반영됨.
            // 사용자 명시적 새로고침은 HistoryScreen 의 PullToRefreshBox → HistoryIntent.Refresh.
            repeatOnSubscription {
                // 자동 재진입 — 다이얼로그가 떠 있으면 갱신 보류 (사용자 dismiss 우선).
                refresh(force = false)
                try {
                    // 구독이 유지되는 동안 awaitCancellation 으로 대기 — 구독 해제(화면 이탈) 시
                    // CancellationException 으로 빠져나와 finally 에서 진행 중 GET 을 정리한다.
                    awaitCancellation()
                } finally {
                    loadJob?.cancel()
                }
            }
        }

    override fun onIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.LoadNext -> loadNext()
            is HistoryIntent.Refresh -> refresh(force = true)
            is HistoryIntent.FilterChanged -> applyFilter(intent.filter)
            is HistoryIntent.DialogDismissed -> dismissDialog()
            is HistoryIntent.DialogConfirmed -> confirmDialog()
        }
    }

    private fun loadNext() {
        loadJob?.cancel()
        loadJob = intent {
            if (state.loadingPage || state.endReached) return@intent
            reduce { state.copy(loadingPage = true, dialog = null) }
            fetchPage()
        }
    }

    /**
     * @param force `false` (자동 재진입) 일 때 처리되지 않은 다이얼로그가 있으면 갱신을 보류한다 —
     * 그렇지 않으면 dialog 가 사용자 입력 없이 사라지면서 다시 로딩 화면이 깜빡거리는 버그가 발생.
     * 사용자 명시 PullToRefresh (`HistoryIntent.Refresh`) 는 `force = true` 로 다이얼로그를 닫고 새로 시도.
     */
    private fun refresh(force: Boolean) {
        loadJob?.cancel()
        loadJob = intent {
            if (!force && state.dialog != null) return@intent
            reduce {
                state.copy(
                    items = emptyList(),
                    nextCursor = null,
                    endReached = false,
                    isRefreshing = true,
                    dialog = null,
                )
            }
            fetchPage()
        }
    }

    private fun applyFilter(filter: LogFilter) {
        loadJob?.cancel()
        loadJob = intent {
            reduce {
                state.copy(
                    filter = filter,
                    items = emptyList(),
                    nextCursor = null,
                    endReached = false,
                    isRefreshing = true,
                    dialog = null,
                )
            }
            fetchPage()
        }
    }

    private suspend fun Syntax<HistoryUiState, Nothing>.fetchPage() {
        when (val result = getAccessLogsUseCase(state.filter, state.nextCursor)) {
            is Outcome.Failure -> reduce {
                state.copy(
                    loadingPage = false,
                    isRefreshing = false,
                    dialog = DialogState.Error(result.error.toUiError()),
                )
            }
            is Outcome.Success -> {
                val page = result.data
                reduce {
                    state.copy(
                        loadingPage = false,
                        isRefreshing = false,
                        items = state.items + page.items,
                        nextCursor = page.nextCursor,
                        endReached = !page.hasMore,
                    )
                }
            }
        }
    }

    private fun dismissDialog() = intent {
        reduce { state.copy(dialog = null) }
    }

    /**
     * 다이얼로그의 [UiError.Action] 을 소비해 처리한다 (Reauthenticate → LogoutUseCase 직접 호출).
     * 본 화면은 외부 SideEffect 가 없어 BaseViewModel 의 SideEffect 슬롯이 [Nothing] 이다.
     */
    private fun confirmDialog() = intent {
        val action = (state.dialog as? DialogState.Error)?.uiError?.action
        reduce { state.copy(dialog = null) }
        if (action != null) fireAction(action)
    }

    private suspend fun fireAction(action: UiError.Action) {
        when (action) {
            // 토큰 폐기만 책임 — Navigation 은 AuthRepository.authState 관찰자(AccessNavGraph) 가 처리.
            is UiError.Action.Reauthenticate -> logoutUseCase()
            // History 화면은 NFC 와 무관하므로 OpenNfcSettings 가 도달하지 않는다 — no-op.
            is UiError.Action.OpenNfcSettings,
            is UiError.Action.None -> Unit
        }
    }
}
