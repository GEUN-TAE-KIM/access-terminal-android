package com.gtkim.mobile_access_control.navigation

import com.gtkim.mobile_access_control.component.auth.domain.usecase.LogoutUseCase
import com.gtkim.mobile_access_control.component.sync.domain.model.FlushState
import com.gtkim.mobile_access_control.component.sync.domain.usecase.GetPendingCountUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveNetworkStateUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveOfflineFlushStateUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveQueueDeadLetterUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveQueueOverflowUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.SyncNowUseCase
import com.gtkim.mobile_access_control.R
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class AppShellViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // openSyncConfirm() 이 online/offline 에서 만드는 다이얼로그와 1:1 — expectState 정확 비교용.
    private val syncConfirmDialog = DialogState.Confirm(
        title = UiText.Res(R.string.topbar_sync_confirm_title),
        message = UiText.Res(R.string.topbar_sync_confirm_message),
        confirmLabel = UiText.Res(R.string.topbar_sync_confirm_action),
    )

    private val syncOfflineDialog = DialogState.Info(
        title = UiText.Res(R.string.topbar_sync_offline_title),
        message = UiText.Res(R.string.topbar_sync_offline_message),
    )

    private fun makeViewModel(
        pendingCount: GetPendingCountUseCase = mockk { every { this@mockk() } returns flowOf(0) },
        observeNetworkState: ObserveNetworkStateUseCase = mockk { every { this@mockk() } returns flowOf(true) },
        syncNow: SyncNowUseCase = mockk(relaxed = true),
        observeFlushState: ObserveOfflineFlushStateUseCase = mockk {
            every { this@mockk() } returns flowOf(FlushState.Idle)
        },
        // emptyFlow() — overflow/dead-letter 미발생 시나리오 기본값. hot SharedFlow 로 mock 하면
        // container 의 collect 가 끝나지 않아 orbit-test 가 timeout 으로 잡는다. emit 검증이 필요한
        // 테스트는 개별적으로 MutableSharedFlow 를 override 한다.
        observeQueueOverflow: ObserveQueueOverflowUseCase = mockk {
            every { this@mockk() } returns emptyFlow()
        },
        observeQueueDeadLetter: ObserveQueueDeadLetterUseCase = mockk {
            every { this@mockk() } returns emptyFlow()
        },
        logoutUseCase: LogoutUseCase = mockk(relaxed = true),
    ): AppShellViewModel = AppShellViewModel(
        pendingCount,
        observeNetworkState,
        syncNow,
        observeFlushState,
        observeQueueOverflow,
        observeQueueDeadLetter,
        logoutUseCase,
    )

    @Test
    fun `pendingCount observation updates state`() = runTest {
        val pendingCount = mockk<GetPendingCountUseCase> { every { this@mockk() } returns flowOf(5) }

        makeViewModel(pendingCount = pendingCount).test(this) {
            runOnCreate()
            expectState { copy(pendingCount = 5) }
        }
    }

    @Test
    fun `network state observation updates state`() = runTest {
        val observeNetworkState = mockk<ObserveNetworkStateUseCase> { every { this@mockk() } returns flowOf(false) }

        makeViewModel(observeNetworkState = observeNetworkState).test(this) {
            runOnCreate()
            expectState { copy(isOnline = false) }
        }
    }

    @Test
    fun `queue overflow triggers side effect`() = runTest {
        val observeQueueOverflow = mockk<ObserveQueueOverflowUseCase> {
            every { this@mockk() } returns flowOf(Unit)
        }

        makeViewModel(observeQueueOverflow = observeQueueOverflow).test(this) {
            runOnCreate()
            expectSideEffect(AppShellSideEffect.ShowQueueOverflow)
        }
    }

    @Test
    fun `queue dead letter triggers side effect`() = runTest {
        val observeQueueDeadLetter = mockk<ObserveQueueDeadLetterUseCase> {
            every { this@mockk() } returns flowOf(Unit)
        }

        makeViewModel(observeQueueDeadLetter = observeQueueDeadLetter).test(this) {
            runOnCreate()
            expectSideEffect(AppShellSideEffect.ShowQueueDeadLetter)
        }
    }

    @Test
    fun `RequestSync opens confirm dialog when online`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            containerHost.onIntent(AppShellIntent.RequestSync)
            expectState { copy(dialog = syncConfirmDialog) }
        }
    }

    @Test
    fun `RequestSync opens info dialog when offline`() = runTest {
        val observeNetworkState = mockk<ObserveNetworkStateUseCase> { every { this@mockk() } returns flowOf(false) }

        makeViewModel(observeNetworkState = observeNetworkState).test(this) {
            runOnCreate()
            expectState { copy(isOnline = false) }
            containerHost.onIntent(AppShellIntent.RequestSync)
            expectState { copy(dialog = syncOfflineDialog) }
        }
    }

    @Test
    fun `ConfirmSync triggers sync-now when online`() = runTest {
        val syncNow = mockk<SyncNowUseCase>(relaxed = true)
        val flushFlow = MutableStateFlow(FlushState.Idle)
        val observeFlushState = mockk<ObserveOfflineFlushStateUseCase> {
            every { this@mockk() } returns flushFlow
        }

        makeViewModel(
            syncNow = syncNow,
            observeFlushState = observeFlushState,
        ).test(this) {
            runOnCreate()
            containerHost.onIntent(AppShellIntent.RequestSync)
            expectState { copy(dialog = syncConfirmDialog) }
            containerHost.onIntent(AppShellIntent.ConfirmSync)
            expectState { copy(dialog = null) }

            verify { syncNow() }

            flushFlow.value = FlushState.Succeeded
            expectSideEffect(AppShellSideEffect.ShowFlushSucceeded)
        }
    }

    @Test
    fun `flush failure triggers ShowFlushFailed side effect`() = runTest {
        val flushFlow = MutableStateFlow(FlushState.Idle)
        val observeFlushState = mockk<ObserveOfflineFlushStateUseCase> {
            every { this@mockk() } returns flushFlow
        }

        makeViewModel(observeFlushState = observeFlushState).test(this) {
            runOnCreate()
            containerHost.onIntent(AppShellIntent.RequestSync)
            expectState { copy(dialog = syncConfirmDialog) }
            containerHost.onIntent(AppShellIntent.ConfirmSync)
            expectState { copy(dialog = null) }

            flushFlow.value = FlushState.Failed
            expectSideEffect(AppShellSideEffect.ShowFlushFailed)
        }
    }

    @Test
    fun `DismissDialog clears dialog`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            containerHost.onIntent(AppShellIntent.RequestSync)
            expectState { copy(dialog = syncConfirmDialog) }
            containerHost.onIntent(AppShellIntent.DismissDialog)
            expectState { copy(dialog = null) }
        }
    }

    @Test
    fun `Logout triggers logout use case`() = runTest {
        val logoutUseCase = mockk<LogoutUseCase>(relaxed = true)

        makeViewModel(logoutUseCase = logoutUseCase).test(this) {
            runOnCreate()
            containerHost.onIntent(AppShellIntent.Logout)
        }

        coVerify { logoutUseCase() }
    }
}
