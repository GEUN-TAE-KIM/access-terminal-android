package com.gtkim.mobile_access_control.feature.stats

import com.gtkim.mobile_access_control.component.auth.domain.usecase.LogoutUseCase
import com.gtkim.mobile_access_control.component.stats.domain.model.DailyStats
import com.gtkim.mobile_access_control.component.stats.domain.model.StatsError
import com.gtkim.mobile_access_control.component.stats.domain.model.StatsSummary
import com.gtkim.mobile_access_control.component.stats.domain.usecase.GetDailyStatsUseCase
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.feature.stats.ui.StatsIntent
import com.gtkim.mobile_access_control.feature.stats.ui.StatsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.test.test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** 시각 고정 — JST 기준 today() 가 결정적으로 2026-05-17 이 되도록 한다. */
    private class FakeTimeProvider(private val fixed: Instant) : TimeProvider {
        override fun now(): Instant = fixed
        override fun zoneId(): ZoneId = ZoneId.of("Asia/Tokyo")
    }

    private val today: LocalDate = LocalDate.of(2026, 5, 17)
    private val fixedNow: Instant =
        today.atTime(12, 0).atZone(ZoneId.of("Asia/Tokyo")).toInstant()

    private fun stats(date: LocalDate) = DailyStats(
        date = date,
        summary = StatsSummary(totalAttempts = 12, allowed = 10, denied = 2, allowedRate = 0.833),
        byHour = emptyList(),
        byDenyReason = emptyList(),
    )

    private fun makeViewModel(
        getDailyStats: GetDailyStatsUseCase = mockk<GetDailyStatsUseCase>().also {
            coEvery { it(any()) } returns Outcome.Success(stats(today))
        },
        logout: LogoutUseCase = mockk(relaxed = true),
        timeProvider: TimeProvider = FakeTimeProvider(fixedNow),
    ): StatsViewModel = StatsViewModel(getDailyStats, logout, timeProvider)

    @Test
    fun `on subscription loads stats for today`() = runTest {
        val getDailyStats = mockk<GetDailyStatsUseCase>()
        coEvery { getDailyStats(today) } returns Outcome.Success(stats(today))

        makeViewModel(getDailyStats).test(this) {
            runOnCreate()
            expectState { copy(loading = false, stats = stats(today)) }
            // onCreate 의 repeatOnSubscription { ...; awaitCancellation() } 가 끝나지 않으므로
            // 명시적으로 컨테이너를 정리해 orbit-test 의 "남은 intent 대기" 타임아웃을 피한다.
            cancelAndIgnoreRemainingItems()
        }

        coVerify(exactly = 1) { getDailyStats(today) }
    }

    @Test
    fun `DateSelected loads stats for the chosen date`() = runTest {
        val chosen = LocalDate.of(2026, 5, 10)
        val getDailyStats = mockk<GetDailyStatsUseCase>()
        coEvery { getDailyStats(today) } returns Outcome.Success(stats(today))
        coEvery { getDailyStats(chosen) } returns Outcome.Success(stats(chosen))

        makeViewModel(getDailyStats).test(this) {
            runOnCreate()
            expectState { copy(loading = false, stats = stats(today)) }
            containerHost.onIntent(StatsIntent.DateSelected(chosen))
            expectState { copy(date = chosen, loading = true) }
            expectState { copy(date = chosen, loading = false, stats = stats(chosen)) }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun `load failure surfaces an error dialog`() = runTest {
        val getDailyStats = mockk<GetDailyStatsUseCase>()
        coEvery { getDailyStats(any()) } returns
            Outcome.Failure(StatsError.Unknown(RuntimeException("stats unavailable")))

        makeViewModel(getDailyStats).test(this) {
            runOnCreate()
            expectState {
                copy(
                    loading = false,
                    dialog = DialogState.Error(
                        // raw cause.message 가 사용자에게 노출되지 않도록 일반 문구로 통일됨.
                        // 매퍼는 StatsError.Unknown.message (UiText.Res) 를 그대로 전달.
                        UiError(
                            title = UiText.Res(R.string.stats_error_unknown_title),
                            message = StatsError.Unknown(RuntimeException()).message,
                        ),
                    ),
                )
            }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun `Refresh reloads stats for the current date`() = runTest {
        val getDailyStats = mockk<GetDailyStatsUseCase>()
        coEvery { getDailyStats(today) } returns Outcome.Success(stats(today))

        makeViewModel(getDailyStats).test(this) {
            runOnCreate()
            expectState { copy(loading = false, stats = stats(today)) }
            containerHost.onIntent(StatsIntent.Refresh)
            expectState { copy(loading = true) }
            expectState { copy(loading = false, stats = stats(today)) }
            cancelAndIgnoreRemainingItems()
        }

        coVerify(exactly = 2) { getDailyStats(today) }
    }

    @Test
    fun `DialogDismissed clears the error dialog`() = runTest {
        val getDailyStats = mockk<GetDailyStatsUseCase>()
        coEvery { getDailyStats(any()) } returns
            Outcome.Failure(StatsError.Unknown(RuntimeException("stats unavailable")))

        makeViewModel(getDailyStats).test(this) {
            runOnCreate()
            expectState {
                copy(
                    loading = false,
                    dialog = DialogState.Error(
                        // raw cause.message 가 사용자에게 노출되지 않도록 일반 문구로 통일됨.
                        // 매퍼는 StatsError.Unknown.message (UiText.Res) 를 그대로 전달.
                        UiError(
                            title = UiText.Res(R.string.stats_error_unknown_title),
                            message = StatsError.Unknown(RuntimeException()).message,
                        ),
                    ),
                )
            }
            containerHost.onIntent(StatsIntent.DialogDismissed)
            expectState { copy(dialog = null) }
            cancelAndIgnoreRemainingItems()
        }
    }
}
