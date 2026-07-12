package com.gtkim.mobile_access_control.feature.login

import com.gtkim.mobile_access_control.component.auth.domain.model.Admin
import com.gtkim.mobile_access_control.component.auth.domain.model.AdminRole
import com.gtkim.mobile_access_control.component.auth.domain.model.AuthError
import com.gtkim.mobile_access_control.component.auth.domain.usecase.LoginUseCase
import com.gtkim.mobile_access_control.component.master.domain.model.MasterError
import com.gtkim.mobile_access_control.component.master.domain.usecase.SyncMasterDataUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveNetworkStateUseCase
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.feature.login.ui.LoginIntent
import com.gtkim.mobile_access_control.feature.login.ui.LoginSideEffect
import com.gtkim.mobile_access_control.feature.login.ui.LoginViewModel
import com.gtkim.mobile_access_control.core.common.error.UiText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val admin = Admin(id = 1L, username = "admin", name = "관리자", role = AdminRole.ADMIN)

    // AuthError.InvalidCredentials.message 는 이제 UiText.Res 라 그대로 전달.
    private val invalidCredentialsDialog = DialogState.Error(
        UiError(
            title = UiText.Res(R.string.login_error_invalid_credentials_title),
            message = AuthError.InvalidCredentials.message,
        ),
    )

    private fun makeViewModel(
        login: LoginUseCase = mockk(relaxed = true),
        syncMasterData: SyncMasterDataUseCase = mockk<SyncMasterDataUseCase>().also {
            coEvery { it() } returns Outcome.Success(Unit)
        },
        // emptyFlow — 방출 없음 → state.isOnline 이 기본값(true) 유지. 기본 online 시나리오.
        // offline 가드는 별도 테스트에서 검증.
        observeNetworkState: ObserveNetworkStateUseCase = mockk<ObserveNetworkStateUseCase>().also {
            every { it() } returns emptyFlow()
        },
    ): LoginViewModel = LoginViewModel(login, syncMasterData, observeNetworkState)

    @Test
    fun `IdChanged updates loginId`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            containerHost.onIntent(LoginIntent.IdChanged("admin"))
            expectState { copy(loginId = "admin") }
        }
    }

    @Test
    fun `PasswordChanged updates password`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            containerHost.onIntent(LoginIntent.PasswordChanged("secret"))
            expectState { copy(password = "secret") }
        }
    }

    @Test
    fun `Submit success awaits master sync then navigates to scan`() = runTest {
        val login = mockk<LoginUseCase>()
        coEvery { login("admin", "secret") } returns Outcome.Success(admin)
        val syncMasterData = mockk<SyncMasterDataUseCase>()
        coEvery { syncMasterData() } returns Outcome.Success(Unit)

        makeViewModel(login = login, syncMasterData = syncMasterData).test(this) {
            runOnCreate()
            containerHost.onIntent(LoginIntent.IdChanged("admin"))
            expectState { copy(loginId = "admin") }
            containerHost.onIntent(LoginIntent.PasswordChanged("secret"))
            expectState { copy(password = "secret") }
            containerHost.onIntent(LoginIntent.Submit)
            expectState { copy(loading = true) }
            expectState { copy(loading = false) }
            expectSideEffect(LoginSideEffect.NavigateToScan)
        }

        coVerify(exactly = 1) { syncMasterData() }
    }

    @Test
    fun `Submit success proceeds even when master sync fails (stale cache fallback)`() = runTest {
        val login = mockk<LoginUseCase>()
        coEvery { login("admin", "secret") } returns Outcome.Success(admin)
        val syncMasterData = mockk<SyncMasterDataUseCase>()
        coEvery { syncMasterData() } returns Outcome.Failure(MasterError.NoConnection)

        makeViewModel(login = login, syncMasterData = syncMasterData).test(this) {
            runOnCreate()
            containerHost.onIntent(LoginIntent.IdChanged("admin"))
            expectState { copy(loginId = "admin") }
            containerHost.onIntent(LoginIntent.PasswordChanged("secret"))
            expectState { copy(password = "secret") }
            containerHost.onIntent(LoginIntent.Submit)
            expectState { copy(loading = true) }
            expectState { copy(loading = false) }
            // sync 실패해도 진입은 허용 — 이전 sync 의 stale cache fallback.
            expectSideEffect(LoginSideEffect.NavigateToScan)
        }
    }

    @Test
    fun `Submit failure shows error dialog and clears loading`() = runTest {
        val login = mockk<LoginUseCase>()
        coEvery { login(any(), any()) } returns Outcome.Failure(AuthError.InvalidCredentials)

        makeViewModel(login = login).test(this) {
            runOnCreate()
            containerHost.onIntent(LoginIntent.Submit)
            expectState { copy(loading = true) }
            expectState { copy(loading = false, dialog = invalidCredentialsDialog) }
        }
    }

    @Test
    fun `DialogDismissed clears the error dialog`() = runTest {
        val login = mockk<LoginUseCase>()
        coEvery { login(any(), any()) } returns Outcome.Failure(AuthError.InvalidCredentials)

        makeViewModel(login = login).test(this) {
            runOnCreate()
            containerHost.onIntent(LoginIntent.Submit)
            expectState { copy(loading = true) }
            expectState { copy(loading = false, dialog = invalidCredentialsDialog) }
            containerHost.onIntent(LoginIntent.DialogDismissed)
            expectState { copy(dialog = null) }
        }
    }
}
