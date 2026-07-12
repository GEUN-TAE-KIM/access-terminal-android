package com.gtkim.mobile_access_control.feature.history

import com.gtkim.mobile_access_control.component.history.domain.model.AccessLog
import com.gtkim.mobile_access_control.component.history.domain.model.HistoryError
import com.gtkim.mobile_access_control.component.history.domain.model.LogAdmin
import com.gtkim.mobile_access_control.component.history.domain.model.LogCursor
import com.gtkim.mobile_access_control.component.history.domain.model.LogFilter
import com.gtkim.mobile_access_control.component.history.domain.model.LogPage
import com.gtkim.mobile_access_control.component.history.domain.model.LogResult
import com.gtkim.mobile_access_control.component.history.domain.model.LogResultFilter
import com.gtkim.mobile_access_control.component.auth.domain.usecase.LogoutUseCase
import com.gtkim.mobile_access_control.component.history.domain.usecase.GetAccessLogsUseCase
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.TerminalId
import com.gtkim.mobile_access_control.core.model.Zone
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.feature.history.ui.HistoryIntent
import com.gtkim.mobile_access_control.feature.history.ui.HistoryViewModel
import com.gtkim.mobile_access_control.core.common.error.UiText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.test.test
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeTimeProvider : TimeProvider {
        override fun now(): Instant = Instant.EPOCH
        override fun zoneId(): ZoneId = ZoneId.of("Asia/Tokyo")
    }

    private fun log(id: Long) = AccessLog(
        id = id,
        cardUid = CardUid("UID-$id"),
        cardType = "MOCK",
        result = LogResult.ALLOWED,
        denyReason = null,
        user = null,
        admin = LogAdmin(id = 1L, username = "admin", name = "現場管理者"),
        terminalId = TerminalId("MOBILE-001"),
        zone = Zone("LOBBY"),
        attemptedAt = Instant.ofEpochSecond(0),
    )

    private fun page(items: List<AccessLog>, cursor: LogCursor?) =
        LogPage(items = items, nextCursor = cursor, hasMore = cursor != null)

    /**
     * 기본 mock 은 빈 페이지를 반환한다 — container 의 repeatOnSubscription { refresh() } 가
     * 구독 시작과 함께 첫 로드를 트리거하므로, getLogs 가 항상 응답할 수 있어야 한다.
     */
    private fun makeViewModel(
        getLogs: GetAccessLogsUseCase = mockk<GetAccessLogsUseCase>().also {
            coEvery { it(any(), any()) } returns Outcome.Success(page(emptyList(), null))
        },
        logout: LogoutUseCase = mockk(relaxed = true),
        timeProvider: TimeProvider = FakeTimeProvider(),
    ): HistoryViewModel = HistoryViewModel(getLogs, logout, timeProvider)

    @Test
    fun `on subscription refresh loads the first page`() = runTest {
        val getLogs = mockk<GetAccessLogsUseCase>()
        coEvery { getLogs(LogFilter(), null) } returns Outcome.Success(page(listOf(log(1L)), null))

        makeViewModel(getLogs).test(this) {
            runOnCreate()
            expectState {
                copy(
                    isRefreshing = false,
                    loadingPage = false,
                    items = listOf(log(1L)),
                    nextCursor = null,
                    endReached = true,
                )
            }
            cancelAndIgnoreRemainingItems()
        }

        coVerify(exactly = 1) { getLogs(LogFilter(), null) }
    }

    @Test
    fun `LoadNext appends the next page and marks end when cursor is null`() = runTest {
        val cursor = LogCursor("c1")
        val getLogs = mockk<GetAccessLogsUseCase>()
        coEvery { getLogs(LogFilter(), null) } returns Outcome.Success(page(listOf(log(1L)), cursor))
        coEvery { getLogs(LogFilter(), cursor) } returns Outcome.Success(page(listOf(log(2L)), null))

        makeViewModel(getLogs).test(this) {
            runOnCreate()
            expectState {
                copy(
                    isRefreshing = false,
                    loadingPage = false,
                    items = listOf(log(1L)),
                    nextCursor = cursor,
                    endReached = false,
                )
            }
            containerHost.onIntent(HistoryIntent.LoadNext)
            expectState { copy(loadingPage = true) }
            expectState {
                copy(
                    loadingPage = false,
                    items = listOf(log(1L), log(2L)),
                    nextCursor = null,
                    endReached = true,
                )
            }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun `LoadNext is ignored once the end is reached`() = runTest {
        val getLogs = mockk<GetAccessLogsUseCase>()
        coEvery { getLogs(any(), any()) } returns Outcome.Success(page(emptyList(), null))

        makeViewModel(getLogs).test(this) {
            runOnCreate()
            expectState { copy(isRefreshing = false, loadingPage = false, endReached = true) }
            // endReached 가드로 추가 전이 없음.
            containerHost.onIntent(HistoryIntent.LoadNext)
            cancelAndIgnoreRemainingItems()
        }

        coVerify(exactly = 1) { getLogs(any(), any()) }
    }

    @Test
    fun `loadNext failure surfaces an error dialog`() = runTest {
        val getLogs = mockk<GetAccessLogsUseCase>()
        coEvery { getLogs(any(), any()) } returns
            Outcome.Failure(HistoryError.Unknown(RuntimeException("network down")))

        makeViewModel(getLogs).test(this) {
            runOnCreate()
            expectState {
                copy(
                    isRefreshing = false,
                    loadingPage = false,
                    dialog = DialogState.Error(
                        // raw cause.message 가 사용자에게 노출되지 않도록 일반 문구로 통일됨.
                        // 매퍼는 HistoryError.Unknown.message (UiText.Res) 를 그대로 전달.
                        UiError(
                            title = UiText.Res(R.string.history_error_unknown_title),
                            message = HistoryError.Unknown(RuntimeException()).message,
                        ),
                    ),
                )
            }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun `FilterChanged resets paging and reloads with the new filter`() = runTest {
        val filter = LogFilter(result = LogResultFilter.DENIED)
        val getLogs = mockk<GetAccessLogsUseCase>()
        coEvery { getLogs(any(), any()) } returns Outcome.Success(page(emptyList(), null))

        makeViewModel(getLogs).test(this) {
            runOnCreate()
            expectState { copy(isRefreshing = false, loadingPage = false, endReached = true) }
            containerHost.onIntent(HistoryIntent.FilterChanged(filter))
            expectState { copy(filter = filter, isRefreshing = true, endReached = false) }
            expectState { copy(filter = filter, isRefreshing = false, endReached = true) }
            cancelAndIgnoreRemainingItems()
        }

        coVerify(exactly = 1) { getLogs(filter, null) }
    }
}
