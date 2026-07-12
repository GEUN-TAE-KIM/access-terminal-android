package com.gtkim.mobile_access_control.feature.register.ui

import android.nfc.Tag
import com.gtkim.mobile_access_control.component.access.domain.model.RegisteredCard
import com.gtkim.mobile_access_control.component.auth.domain.usecase.LogoutUseCase
import com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError
import com.gtkim.mobile_access_control.component.scan.domain.usecase.RegisterScannedCardUseCase
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.core.common.result.AppError
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.feature.common.ui.BaseViewModel
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.feature.register.R
import com.gtkim.mobile_access_control.feature.register.mapper.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
internal class RegisterViewModel @Inject constructor(
    private val registerScannedCardUseCase: RegisterScannedCardUseCase,
    private val logoutUseCase: LogoutUseCase,
) : BaseViewModel<RegisterUiState, RegisterSideEffect, RegisterIntent>() {

    override val container: Container<RegisterUiState, RegisterSideEffect> =
        container(RegisterUiState())

    override fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.EmployeeCodeChanged -> setEmployeeCode(intent.raw)
            is RegisterIntent.StartScan -> startScan()
            is RegisterIntent.CancelScan -> cancelScan()
            is RegisterIntent.TagDetected -> handleTagDetected(intent.tag)
            is RegisterIntent.NfcUnavailable -> showError(NfcError.NotSupported.toUiError())
            is RegisterIntent.NfcDisabled -> showError(NfcError.Disabled.toUiError())
            is RegisterIntent.ResultConfirmed -> resetToIdle()
            is RegisterIntent.DialogDismissed -> dismissDialog()
            is RegisterIntent.DialogConfirmed -> confirmDialog()
        }
    }

    private fun setEmployeeCode(raw: String) = intent {
        if (state.phase != RegisterUiState.Phase.IDLE) return@intent
        reduce { state.copy(employeeCodeInput = raw) }
    }

    private fun startScan() = intent {
        if (state.phase != RegisterUiState.Phase.IDLE) return@intent
        if (state.selectedEmployee == null || state.dialog != null) return@intent
        reduce { state.copy(phase = RegisterUiState.Phase.SCANNING) }
    }

    private fun cancelScan() = intent {
        if (state.phase == RegisterUiState.Phase.IDLE) return@intent
        reduce { state.copy(phase = RegisterUiState.Phase.IDLE) }
    }

    private fun handleTagDetected(tag: Tag) = intent {
        if (state.phase != RegisterUiState.Phase.SCANNING) return@intent
        val employeeCode = state.selectedEmployee ?: return@intent

        reduce { state.copy(phase = RegisterUiState.Phase.REGISTERING) }

        val outcome: Outcome<RegisteredCard, AppError> =
            registerScannedCardUseCase(tag, employeeCode)

        when (outcome) {
            is Outcome.Failure -> reduce {
                state.copy(
                    phase = RegisterUiState.Phase.IDLE,
                    dialog = DialogState.Error(outcome.error.toUiError()),
                )
            }

            is Outcome.Success -> reduce {
                state.copy(
                    phase = RegisterUiState.Phase.DONE,
                    dialog = outcome.data.toSuccessDialog(),
                )
            }
        }
    }

    private fun showError(uiError: UiError) = intent {
        reduce {
            state.copy(
                phase = RegisterUiState.Phase.IDLE,
                dialog = DialogState.Error(uiError),
            )
        }
    }

    private fun resetToIdle() = intent {
        reduce {
            state.copy(
                phase = RegisterUiState.Phase.IDLE,
                dialog = null,
            )
        }
    }

    private fun dismissDialog() = intent {
        if (state.dialog != null) {
            reduce {
                state.copy(
                    phase = RegisterUiState.Phase.IDLE,
                    dialog = null,
                )
            }
        }
    }

    private fun confirmDialog() = intent {
        val action = (state.dialog as? DialogState.Error)?.uiError?.action

        reduce {
            state.copy(
                phase = RegisterUiState.Phase.IDLE,
                dialog = null,
            )
        }

        when (action) {
            is UiError.Action.OpenNfcSettings -> postSideEffect(RegisterSideEffect.OpenNfcSettings)
            is UiError.Action.Reauthenticate -> logoutUseCase()
            is UiError.Action.None, null -> Unit
        }
    }
}

// TODO: 성공 다이얼로그 구성 위치를 다른 VM 과 통일 — scan 등은 Success 분기 reduce 안에서 인라인 구성한다.
/** 등록 성공 안내 — 에러가 아니므로 Info 다이얼로그. 확인 시 RegisterScreen 의 DialogConfirmed → IDLE 복귀. */
private fun RegisteredCard.toSuccessDialog(): DialogState.Info = DialogState.Info(
    title = UiText.Res(R.string.register_success_title),
    message = UiText.FormattedRes(
        R.string.register_success_message,
        user.name,
        user.employeeCode.value,
        cardUid.value,
    ),
)
