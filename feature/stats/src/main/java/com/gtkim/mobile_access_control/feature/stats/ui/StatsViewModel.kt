package com.gtkim.mobile_access_control.feature.stats.ui

import com.gtkim.mobile_access_control.component.auth.domain.usecase.LogoutUseCase
import com.gtkim.mobile_access_control.component.stats.domain.usecase.GetDailyStatsUseCase
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.feature.common.ui.BaseViewModel
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.feature.stats.mapper.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class StatsViewModel @Inject constructor(
    private val getDailyStatsUseCase: GetDailyStatsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val timeProvider: TimeProvider,
) : BaseViewModel<StatsUiState, Nothing, StatsIntent>() {

    /**
     * 진행 중인 GET 의 코루틴 Job 추적용. 새 load 시작 전에 이전 작업을 cancel 한다 —
     * 사용자가 화면을 떠난 동안 살아남아 timeout 응답으로 dialog 를 덮어쓰는 stale write 차단.
     */
    private var loadJob: Job? = null

    // initial loading = true → 첫 composition 부터 fullscreen 로딩 표시 (History 와 동일 이유).
    override val container: Container<StatsUiState, Nothing> = container(
        StatsUiState(date = today(), loading = true)
    ) {
        // 화면 재진입마다 현재 선택된 날짜 기준으로 새로 로드.
        // 자정 너머 머무는 경우 today() 가 바뀐 것을 다음 진입에 자연스럽게 반영.
        repeatOnSubscription {
            // 자동 재진입 — 현재 state.date 그대로 사용, 다이얼로그가 떠 있으면 갱신 보류.
            load(targetDate = null, force = false)
            try {
                // 구독이 유지되는 동안 대기 — 구독 해제(화면 이탈) 시 finally 에서 진행 중 GET 정리.
                awaitCancellation()
            } finally {
                loadJob?.cancel()
            }
        }
    }

    override fun onIntent(intent: StatsIntent) {
        when (intent) {
            is StatsIntent.DateSelected -> load(targetDate = intent.date, force = true)
            is StatsIntent.Refresh -> load(targetDate = null, force = true)
            is StatsIntent.DialogDismissed -> dismissDialog()
            is StatsIntent.DialogConfirmed -> confirmDialog()
        }
    }

    // LocalDate.ofInstant 는 Android API 34+ 에서만 제공되어 그 미만 단말에서 NoSuchMethodError
    // 로 즉시 크래시. minSdk 26 을 지원하려면 Instant → ZonedDateTime → LocalDate 경로를 쓴다.
    private fun today(): LocalDate = timeProvider.now().atZone(timeProvider.zoneId()).toLocalDate()

    /**
     * @param targetDate 새 날짜로 전환할 때 지정, `null` 이면 현재 `state.date` 유지.
     * @param force `false` (자동 재진입) 일 때 처리되지 않은 다이얼로그가 있으면 갱신을 보류한다 —
     * 그렇지 않으면 dialog 가 사용자 입력 없이 사라지면서 다시 로딩 화면이 깜빡거리는 버그가 발생.
     * 사용자 명시 액션(DateSelected/Refresh) 은 `force = true` 로 다이얼로그를 닫고 새로 시도.
     */
    private fun load(targetDate: LocalDate?, force: Boolean) {
        loadJob?.cancel()
        loadJob = intent {
            if (!force && state.dialog != null) return@intent

            val date = targetDate ?: state.date

            reduce { state.copy(date = date, loading = true, dialog = null) }

            when (val result = getDailyStatsUseCase(date)) {
                is Outcome.Failure -> reduce {
                    state.copy(
                        loading = false,
                        dialog = DialogState.Error(result.error.toUiError()),
                    )
                }

                is Outcome.Success -> reduce { state.copy(loading = false, stats = result.data) }
            }
        }
    }

    private fun dismissDialog() = intent {
        reduce { state.copy(dialog = null) }
    }

    /**
     * 다이얼로그의 [com.gtkim.mobile_access_control.feature.common.ui.error.UiError.Action] 을 소비해 처리한다
     * (Reauthenticate → LogoutUseCase 직접 호출).
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
            is UiError.Action.OpenNfcSettings,
            is UiError.Action.None -> Unit
        }
    }
}
