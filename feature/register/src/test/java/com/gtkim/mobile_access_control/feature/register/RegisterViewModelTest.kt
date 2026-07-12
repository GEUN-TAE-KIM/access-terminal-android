package com.gtkim.mobile_access_control.feature.register

import android.nfc.Tag
import com.gtkim.mobile_access_control.component.access.domain.model.AccessUser
import com.gtkim.mobile_access_control.component.access.domain.model.CardError
import com.gtkim.mobile_access_control.component.access.domain.model.RegisteredCard
import com.gtkim.mobile_access_control.component.auth.domain.usecase.LogoutUseCase
import com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError
import com.gtkim.mobile_access_control.component.scan.domain.usecase.RegisterScannedCardUseCase
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.feature.register.ui.RegisterIntent
import com.gtkim.mobile_access_control.feature.register.ui.RegisterSideEffect
import com.gtkim.mobile_access_control.feature.register.ui.RegisterUiState
import com.gtkim.mobile_access_control.feature.register.ui.RegisterViewModel
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.feature.common.R as CommonR
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val emp001Code = EmployeeCode("EMP001")

    private val registeredCard = RegisteredCard(
        cardUid = CardUid("0123456789ABCDEF"),
        user = AccessUser(
            id = 1L,
            employeeCode = emp001Code,
            name = "山田太郎",
            department = "建設部",
            photoUrl = null,
        ),
    )

    /** OpenNfcSettings 액션을 가진 NfcDisabled 의 표시 UiError — 다이얼로그 confirm 시 SideEffect 트리거. */
    private val nfcDisabledUiError = UiError(
        title = UiText.Res(CommonR.string.error_nfc_title_disabled),
        message = UiText.Res(CommonR.string.error_nfc_message_disabled),
        confirmText = UiText.Res(CommonR.string.error_button_open_settings),
        action = UiError.Action.OpenNfcSettings,
    )

    // 매퍼는 CardAlreadyRegistered 를 통합 "카드 등록 실패" 타이틀로 묶고 message 는 도메인 모델
    // 의 UiText.Res 그대로 노출 (RegisterErrorMapper.toUiError).
    private val cardAlreadyDialog = DialogState.Error(
        UiError(
            title = UiText.Res(R.string.register_error_card_failure_title),
            message = CardError.CardAlreadyRegistered.message,
        ),
    )

    private val unreadableDialog = DialogState.Error(
        UiError(
            title = UiText.Res(CommonR.string.error_nfc_title_unreadable),
            message = UiText.Res(R.string.register_error_nfc_unreadable_message),
        ),
    )

    private fun makeViewModel(
        registerScannedCard: RegisterScannedCardUseCase = mockk(relaxed = true),
        logout: LogoutUseCase = mockk(relaxed = true),
    ): RegisterViewModel = RegisterViewModel(registerScannedCard, logout)

    @Test
    fun `EmployeeCodeChanged sets input while IDLE`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            containerHost.onIntent(RegisterIntent.EmployeeCodeChanged("EMP001"))
            expectState { copy(employeeCodeInput = "EMP001") }
        }
    }

    @Test
    fun `StartScan transitions to SCANNING when employee code is entered`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            containerHost.onIntent(RegisterIntent.EmployeeCodeChanged("EMP001"))
            expectState { copy(employeeCodeInput = "EMP001") }
            containerHost.onIntent(RegisterIntent.StartScan)
            expectState { copy(phase = RegisterUiState.Phase.SCANNING) }
        }
    }

    @Test
    fun `StartScan is ignored when input is blank`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            // employeeCodeInput 이 빈 문자열 — selectedEmployee == null → phase 전이 없음.
            containerHost.onIntent(RegisterIntent.StartScan)
        }
    }

    @Test
    fun `StartScan is ignored when input is whitespace only`() = runTest {
        // 공백만 있는 입력은 trim 후 빈 값 — selectedEmployee 가 null 이라 phase 전이 없다.
        makeViewModel().test(this) {
            runOnCreate()
            containerHost.onIntent(RegisterIntent.EmployeeCodeChanged("   "))
            expectState { copy(employeeCodeInput = "   ") }
            containerHost.onIntent(RegisterIntent.StartScan)
        }
    }

    @Test
    fun `CancelScan resets SCANNING to IDLE`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            containerHost.onIntent(RegisterIntent.EmployeeCodeChanged("EMP001"))
            expectState { copy(employeeCodeInput = "EMP001") }
            containerHost.onIntent(RegisterIntent.StartScan)
            expectState { copy(phase = RegisterUiState.Phase.SCANNING) }
            containerHost.onIntent(RegisterIntent.CancelScan)
            expectState { copy(phase = RegisterUiState.Phase.IDLE) }
        }
    }

    @Test
    fun `TagDetected success registers scanned card and transitions to DONE`() = runTest {
        val tag = mockk<Tag>(relaxed = true)
        val registerScannedCard = mockk<RegisterScannedCardUseCase>()
        coEvery { registerScannedCard(tag, emp001Code) } returns Outcome.Success(registeredCard)

        makeViewModel(registerScannedCard = registerScannedCard).test(this) {
            runOnCreate()
            containerHost.onIntent(RegisterIntent.EmployeeCodeChanged("EMP001"))
            expectState { copy(employeeCodeInput = "EMP001") }
            containerHost.onIntent(RegisterIntent.StartScan)
            expectState { copy(phase = RegisterUiState.Phase.SCANNING) }
            containerHost.onIntent(RegisterIntent.TagDetected(tag))
            expectState { copy(phase = RegisterUiState.Phase.REGISTERING) }
            expectState {
                copy(
                    phase = RegisterUiState.Phase.DONE,
                    dialog = DialogState.Info(
                        title = UiText.Res(R.string.register_success_title),
                        message = UiText.FormattedRes(
                            R.string.register_success_message,
                            registeredCard.user.name,
                            registeredCard.user.employeeCode.value,
                            registeredCard.cardUid.value,
                        ),
                    ),
                )
            }
        }
    }

    @Test
    fun `TagDetected register failure surfaces error dialog and resets to IDLE`() = runTest {
        val tag = mockk<Tag>(relaxed = true)
        val registerScannedCard = mockk<RegisterScannedCardUseCase>()
        coEvery {
            registerScannedCard(tag, emp001Code)
        } returns Outcome.Failure(CardError.CardAlreadyRegistered)

        makeViewModel(registerScannedCard = registerScannedCard).test(this) {
            runOnCreate()
            containerHost.onIntent(RegisterIntent.EmployeeCodeChanged("EMP001"))
            expectState { copy(employeeCodeInput = "EMP001") }
            containerHost.onIntent(RegisterIntent.StartScan)
            expectState { copy(phase = RegisterUiState.Phase.SCANNING) }
            containerHost.onIntent(RegisterIntent.TagDetected(tag))
            expectState { copy(phase = RegisterUiState.Phase.REGISTERING) }
            expectState { copy(phase = RegisterUiState.Phase.IDLE, dialog = cardAlreadyDialog) }
        }
    }

    @Test
    fun `TagDetected read failure surfaces error dialog and resets to IDLE`() = runTest {
        val tag = mockk<Tag>(relaxed = true)
        val registerScannedCard = mockk<RegisterScannedCardUseCase>()
        coEvery { registerScannedCard(tag, emp001Code) } returns Outcome.Failure(NfcError.UnreadableTag)

        makeViewModel(registerScannedCard = registerScannedCard).test(this) {
            runOnCreate()
            containerHost.onIntent(RegisterIntent.EmployeeCodeChanged("EMP001"))
            expectState { copy(employeeCodeInput = "EMP001") }
            containerHost.onIntent(RegisterIntent.StartScan)
            expectState { copy(phase = RegisterUiState.Phase.SCANNING) }
            containerHost.onIntent(RegisterIntent.TagDetected(tag))
            expectState { copy(phase = RegisterUiState.Phase.REGISTERING) }
            expectState { copy(phase = RegisterUiState.Phase.IDLE, dialog = unreadableDialog) }
        }
    }

    @Test
    fun `NfcDisabled from IDLE (pre-flight path) shows NFC disabled dialog`() = runTest {
        // Pre-flight 가드(RegisterScreen) 가 IDLE 상태에서 스캔 버튼을 누른 사용자에게 곧장
        // NfcDisabled 를 발사하는 경로. SCANNING phase 진입 자체가 없어 "카드를 대주세요"
        // progress 다이얼로그가 깜빡이지 않는다.
        makeViewModel().test(this) {
            runOnCreate()
            containerHost.onIntent(RegisterIntent.EmployeeCodeChanged("EMP001"))
            expectState { copy(employeeCodeInput = "EMP001") }
            containerHost.onIntent(RegisterIntent.NfcDisabled)
            expectState {
                copy(
                    phase = RegisterUiState.Phase.IDLE,
                    dialog = DialogState.Error(nfcDisabledUiError),
                )
            }
        }
    }

    @Test
    fun `NfcDisabled while scanning shows error dialog and resets to IDLE`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            containerHost.onIntent(RegisterIntent.EmployeeCodeChanged("EMP001"))
            expectState { copy(employeeCodeInput = "EMP001") }
            containerHost.onIntent(RegisterIntent.StartScan)
            expectState { copy(phase = RegisterUiState.Phase.SCANNING) }
            containerHost.onIntent(RegisterIntent.NfcDisabled)
            expectState {
                copy(
                    phase = RegisterUiState.Phase.IDLE,
                    dialog = DialogState.Error(nfcDisabledUiError),
                )
            }
        }
    }

    @Test
    fun `DialogConfirmed with OpenNfcSettings action posts side effect`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            containerHost.onIntent(RegisterIntent.EmployeeCodeChanged("EMP001"))
            expectState { copy(employeeCodeInput = "EMP001") }
            containerHost.onIntent(RegisterIntent.StartScan)
            expectState { copy(phase = RegisterUiState.Phase.SCANNING) }
            containerHost.onIntent(RegisterIntent.NfcDisabled)
            expectState {
                copy(
                    phase = RegisterUiState.Phase.IDLE,
                    dialog = DialogState.Error(nfcDisabledUiError),
                )
            }
            containerHost.onIntent(RegisterIntent.DialogConfirmed)
            expectState {
                copy(
                    phase = RegisterUiState.Phase.IDLE,
                    dialog = null,
                )
            }
            expectSideEffect(RegisterSideEffect.OpenNfcSettings)
        }
    }
}
