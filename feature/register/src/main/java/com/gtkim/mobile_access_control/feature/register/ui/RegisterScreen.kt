package com.gtkim.mobile_access_control.feature.register.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gtkim.mobile_access_control.feature.common.ui.component.AppButton
import com.gtkim.mobile_access_control.feature.common.ui.dialog.AppDialog
import com.gtkim.mobile_access_control.feature.common.ui.intent.openNfcSettings
import com.gtkim.mobile_access_control.feature.common.ui.nfc.NfcReaderModeEffect
import com.gtkim.mobile_access_control.feature.common.ui.nfc.rememberNfcStartGate
import com.gtkim.mobile_access_control.feature.common.ui.theme.AppTheme
import com.gtkim.mobile_access_control.feature.register.R
import com.gtkim.mobile_access_control.feature.register.ui.component.ScanProgressDialog
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 * Route 레이어 — ViewModel 바인딩 + 플랫폼 effect 조립.
 *
 * 검문 화면과 동일하게 NFC reader mode 는 [NfcReaderModeEffect] 가 캡슐화하며, 스캔 플로우가
 * 진행 중일 때만(active) 활성화된다. Context 가 필요한 SideEffect 라우팅도 본 레이어가 책임진다.
 */
@Composable
fun RegisterScreen() {
    val viewModel: RegisterViewModel = hiltViewModel()
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    val nfcActive = state.phase == RegisterUiState.Phase.SCANNING ||
            state.phase == RegisterUiState.Phase.REGISTERING

    NfcReaderModeEffect(
        active = nfcActive,
        onTagDetected = { tag -> viewModel.onIntent(RegisterIntent.TagDetected(tag)) },
        onUnavailable = { viewModel.onIntent(RegisterIntent.NfcUnavailable) },
        onDisabled = { viewModel.onIntent(RegisterIntent.NfcDisabled) },
    )

    val startGate = rememberNfcStartGate(
        onUnavailable = { viewModel.onIntent(RegisterIntent.NfcUnavailable) },
        onDisabled = { viewModel.onIntent(RegisterIntent.NfcDisabled) },
        onAvailable = { viewModel.onIntent(RegisterIntent.StartScan) },
    )
    val onIntent: (RegisterIntent) -> Unit = { intent ->
        if (intent is RegisterIntent.StartScan) startGate() else viewModel.onIntent(intent)
    }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is RegisterSideEffect.OpenNfcSettings -> context.openNfcSettings()
        }
    }

    RegisterScaffold(state = state, onIntent = onIntent)
}

@Composable
internal fun RegisterScaffold(
    state: RegisterUiState,
    onIntent: (RegisterIntent) -> Unit,
) {
    // 입력 중 텍스트는 로컬 버퍼로 동기 소유 — Orbit reduce 가 비동기라 value 를 state 에 직접
    // 물리면 빠른 입력 시 커서가 튄다. selectedEmployee/스캔 버튼 enable 은 onIntent 포워딩으로 VM 이 유지.
    var employeeInput by remember { mutableStateOf(state.employeeCodeInput) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.register_instruction),
            style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
            value = employeeInput,
            onValueChange = {
                employeeInput = it
                onIntent(RegisterIntent.EmployeeCodeChanged(it))
            },
            label = { Text(stringResource(R.string.register_field_employee_code)) },
            singleLine = true,
            enabled = state.phase == RegisterUiState.Phase.IDLE,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            modifier = Modifier.fillMaxWidth(),
        )

        AppButton(
            text = stringResource(R.string.register_button_scan),
            onClick = { onIntent(RegisterIntent.StartScan) },
            enabled = state.phase == RegisterUiState.Phase.IDLE &&
                    state.selectedEmployee != null &&
                    state.dialog == null,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (state.phase == RegisterUiState.Phase.SCANNING ||
        state.phase == RegisterUiState.Phase.REGISTERING
    ) {
        ScanProgressDialog(
            registering = state.phase == RegisterUiState.Phase.REGISTERING,
            onCancel = { onIntent(RegisterIntent.CancelScan) },
        )
    }

    state.dialog?.let { dialog ->
        AppDialog(
            dialog = dialog,
            onDismiss = { onIntent(RegisterIntent.DialogDismissed) },
            onConfirm = { onIntent(RegisterIntent.DialogConfirmed) },
        )
    }
}

@Preview
@Composable
private fun RegisterScaffoldPreview() {
    AppTheme {
        RegisterScaffold(state = RegisterUiState(), onIntent = {})
    }
}
