package com.gtkim.mobile_access_control.feature.scan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gtkim.mobile_access_control.component.master.domain.model.CachedZone
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardType
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone
import com.gtkim.mobile_access_control.feature.common.ui.component.AppButton
import com.gtkim.mobile_access_control.feature.common.ui.dialog.AppDialog
import com.gtkim.mobile_access_control.feature.common.ui.intent.openNfcSettings
import com.gtkim.mobile_access_control.feature.common.ui.nfc.NfcReaderModeEffect
import com.gtkim.mobile_access_control.feature.common.ui.nfc.rememberNfcStartGate
import com.gtkim.mobile_access_control.feature.common.ui.theme.AppTheme
import com.gtkim.mobile_access_control.feature.scan.BuildConfig
import com.gtkim.mobile_access_control.feature.scan.R
import com.gtkim.mobile_access_control.feature.scan.ui.component.ScanFlowDialog
import com.gtkim.mobile_access_control.feature.scan.ui.component.rememberScanFeedback
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.ZoneId

/**
 * Route 레이어 — ViewModel 바인딩 + 플랫폼 effect 조립.
 */
@Composable
fun ScanScreen() {
    val viewModel: ScanViewModel = hiltViewModel()
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val feedback = rememberScanFeedback()

    val nfcActive = state.phase == ScanUiState.Phase.PROMPTING ||
            (state.phase == ScanUiState.Phase.RESOLVING && !state.mockScan)

    NfcReaderModeEffect(
        active = nfcActive,
        onTagDetected = { tag -> viewModel.onIntent(ScanIntent.TagDetected(tag)) },
        onUnavailable = { viewModel.onIntent(ScanIntent.NfcUnavailable) },
        onDisabled = { viewModel.onIntent(ScanIntent.NfcDisabled) },
    )

    val startGate = rememberNfcStartGate(
        onUnavailable = { viewModel.onIntent(ScanIntent.NfcUnavailable) },
        onDisabled = { viewModel.onIntent(ScanIntent.NfcDisabled) },
        onAvailable = { viewModel.onIntent(ScanIntent.StartScan) },
    )
    val onIntent: (ScanIntent) -> Unit = { intent ->
        if (intent is ScanIntent.StartScan) startGate() else viewModel.onIntent(intent)
    }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is ScanSideEffect.OpenNfcSettings -> context.openNfcSettings()
            is ScanSideEffect.PlayGrantedSound -> feedback.playGranted()
            is ScanSideEffect.PlayDeniedSound -> feedback.playDenied()
        }
    }

    ScanScaffold(state = state, onIntent = onIntent)
}

@Composable
internal fun ScanScaffold(
    state: ScanUiState,
    onIntent: (ScanIntent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ScanLandingPane(
            onStartScan = { onIntent(ScanIntent.StartScan) },
            // zone 미선택이면 시작 버튼 비활성. zone 진입점은 위 [ZoneIndicator] 단일 — 두 진입점
            // (버튼 라벨 분기 + indicator) 으로 사용자 혼란 회피.
            startEnabled = state.phase == ScanUiState.Phase.IDLE &&
                    state.dialog == null &&
                    state.selectedZone != null,
            modifier = Modifier.fillMaxSize(),
        )

        // Zone 표시 + 변경. 미선택 시 "미설정" 강조.
        // 오프라인/큐 상태는 AppShell TopBar에서 전역 표시하므로 여기선 zone만.
        ZoneIndicator(
            selectedZone = state.selectedZone,
            onChangeClick = { onIntent(ScanIntent.OpenZonePicker) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp),
        )

        if (BuildConfig.DEBUG) {
            MockPanel(
                state = state,
                onIntent = onIntent,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
            )
        }
    }

    if (state.phase != ScanUiState.Phase.IDLE) {
        ScanFlowDialog(
            phase = state.phase,
            lastResult = state.lastResult,
            lastError = state.lastError,
            zoneId = state.zoneId,
            onCancel = { onIntent(ScanIntent.CancelScan) },
        )
    }

    state.dialog?.let { dialog ->
        AppDialog(
            dialog = dialog,
            onDismiss = { onIntent(ScanIntent.DialogDismissed) },
            onConfirm = { onIntent(ScanIntent.DialogConfirmed) },
        )
    }

    if (state.showZonePicker) {
        ZonePickerDialog(
            zones = state.availableZones,
            selected = state.selectedZone,
            onSelect = { zone -> onIntent(ScanIntent.SelectZone(zone)) },
            onDismiss = { onIntent(ScanIntent.DismissZonePicker) },
        )
    }
}

/**
 * 현재 단말에 설정된 zone 을 노출 + 변경 버튼.
 *
 * 미선택 시 visual emphasis (errorContainer 배경) — 운영자가 첫 부팅 후 picker 를 잊지 않도록.
 * 선택됐으면 라벨만 담담하게 표시.
 */
@Composable
private fun ZoneIndicator(
    selectedZone: Zone?,
    onChangeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isUnset = selectedZone == null
    Row(
        modifier = modifier
            .background(
                color = if (isUnset) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onChangeClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.scan_zone_label),
            style = MaterialTheme.typography.labelMedium,
            color = if (isUnset) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = selectedZone?.value ?: stringResource(R.string.scan_zone_unselected),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (isUnset) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.scan_zone_change),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Zone picker 다이얼로그 (Phase 12).
 *
 * master sync 로 받은 catalog 를 radio 리스트로 노출. 비어있으면 안내 메시지만 — 운영자가
 * picker 를 닫고 master sync (네트워크 복귀 등) 를 기다린다.
 */
@Composable
private fun ZonePickerDialog(
    zones: List<CachedZone>,
    selected: Zone?,
    onSelect: (Zone) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.scan_zone_picker_title)) },
        text = {
            if (zones.isEmpty()) {
                Text(
                    text = stringResource(R.string.scan_zone_picker_empty),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column {
                    zones.forEach { zone ->
                        val isSelected = zone.code == selected
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = isSelected,
                                    onClick = { onSelect(zone.code) },
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelect(zone.code) },
                            )
                            Column(
                                modifier = Modifier.padding(start = 8.dp),
                            ) {
                                Text(
                                    text = zone.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    text = zone.code.value,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.scan_zone_picker_cancel))
            }
        },
    )
}

@Composable
private fun ScanLandingPane(
    onStartScan: () -> Unit,
    startEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Contactless,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(140.dp),
        )
        Text(
            text = stringResource(R.string.scan_landing_instruction),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 32.dp),
        )
        Button(
            onClick = onStartScan,
            enabled = startEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(),
            contentPadding = ButtonDefaults.ContentPadding,
        ) {
            Text(
                text = stringResource(R.string.scan_button_start),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
    }
}

/**
 * [Debug-only] Mock 카드 패널. 실 NFC 없이 검문 플로우를 검증하기 위한 진입점.
 *
 * architecture.md §5 — Release 빌드 노출 절대 금지. 호출부([ScanScaffold])가 `BuildConfig.DEBUG` 로
 * 가드하므로 본 컴포저블 자체는 빌드 타입을 신경 쓰지 않는다.
 */
@Composable
private fun MockPanel(
    state: ScanUiState,
    onIntent: (ScanIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppButton(
            text = if (state.showMockPanel) {
                stringResource(R.string.scan_mock_panel_close)
            } else {
                stringResource(R.string.scan_mock_panel_open)
            },
            onClick = { onIntent(ScanIntent.MockPanelToggled(!state.showMockPanel)) },
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.showMockPanel) {
            AppButton(
                // Mock 은 NFC read 를 건너뛰고 곧장 verify — PROMPTING 없이 IDLE 에서 한 번에 발사한다.
                // 비-IDLE phase 에서는 ScanFlowDialog(모달)가 패널을 덮으므로 phase 분기는 불필요.
                // 게이트는 실 시작 버튼과 동일: IDLE + zone 선택됨.
                text = stringResource(R.string.scan_mock_start),
                enabled = state.phase == ScanUiState.Phase.IDLE && state.selectedZone != null,
                onClick = {
                    onIntent(ScanIntent.MockCardEmitted(CardUid("EMP001"), CardType.MOCK))
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}


@Preview
@Composable
private fun ScanScaffoldPreview() {
    AppTheme {
        ScanScaffold(
            state = ScanUiState(zoneId = ZoneId.of("Asia/Tokyo"), selectedZone = Zone("GATE-A")),
            onIntent = {},
        )
    }
}
