package com.gtkim.mobile_access_control.feature.scan.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.core.common.time.AppDateTimeFormatter
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.feature.common.ui.error.asString
import com.gtkim.mobile_access_control.feature.common.ui.label.toKoreanLabelRes
import com.gtkim.mobile_access_control.feature.common.ui.nfc.NfcCardPromptIcon
import com.gtkim.mobile_access_control.feature.common.R as CommonR
import com.gtkim.mobile_access_control.feature.scan.R
import com.gtkim.mobile_access_control.feature.scan.ui.ScanUiState
import java.time.ZoneId

/**
 * 검문 플로우의 단일 다이얼로그.
 *
 * Phase 별 컨텐츠 분기:
 * - PROMPTING : 펄스 아이콘 + "카드를 가까이 대주세요"
 * - RESOLVING : 로더 + "카드를 인식하는 중입니다 — 떼지 마세요"
 * - DONE      : lastError 우선 → 에러 화면, 없으면 lastResult 의 허용/거부 표시
 *
 * 한 다이얼로그 안에서 모든 단계를 다루는 이유 — 단계 전환마다 dialog 가 깜빡이지 않고,
 * 사용자 시선이 한 곳에 머문다.
 */
@Composable
internal fun ScanFlowDialog(
    phase: ScanUiState.Phase,
    lastResult: AccessResult?,
    lastError: UiError?,
    zoneId: ZoneId,
    onCancel: () -> Unit,
) {
    val dismissAllowed = phase != ScanUiState.Phase.RESOLVING

    AlertDialog(
        // RESOLVING 중에는 외부 터치/뒤로가기로 닫지 않게 — read/verify 가 절단되면 카드 통신이 깨짐.
        onDismissRequest = { if (dismissAllowed) onCancel() },
        title = { Text(text = titleFor(phase, lastResult, lastError)) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (phase) {
                    ScanUiState.Phase.PROMPTING -> PromptingContent()
                    ScanUiState.Phase.RESOLVING -> ResolvingContent()
                    ScanUiState.Phase.DONE -> when {
                        lastError != null -> ErrorContent(uiError = lastError)
                        lastResult != null -> ResultContent(result = lastResult, zoneId = zoneId)
                        else -> Unit
                    }
                    ScanUiState.Phase.IDLE -> Unit
                }
            }
        },
        confirmButton = {
            when (phase) {
                // RESOLVING 중에는 모든 버튼 숨김 — 인터럽트 금지.
                ScanUiState.Phase.RESOLVING -> Unit
                ScanUiState.Phase.DONE -> TextButton(onClick = onCancel) {
                    Text(stringResource(CommonR.string.common_confirm))
                }
                ScanUiState.Phase.PROMPTING -> TextButton(onClick = onCancel) {
                    Text(stringResource(CommonR.string.common_cancel))
                }
                ScanUiState.Phase.IDLE -> Unit
            }
        },
    )
}

@Composable
private fun titleFor(
    phase: ScanUiState.Phase,
    lastResult: AccessResult?,
    lastError: UiError?,
): String = when (phase) {
    ScanUiState.Phase.PROMPTING -> stringResource(R.string.scan_dialog_title_prompting)
    ScanUiState.Phase.RESOLVING -> stringResource(R.string.scan_dialog_title_resolving)
    ScanUiState.Phase.DONE -> when {
        lastError != null -> lastError.title.asString()
        lastResult?.decision?.isAllowed == true -> stringResource(CommonR.string.common_allowed)
        lastResult != null -> stringResource(CommonR.string.common_denied)
        else -> ""
    }
    ScanUiState.Phase.IDLE -> ""
}

@Composable
private fun PromptingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NfcCardPromptIcon()
        Text(
            text = stringResource(R.string.scan_dialog_body_prompting),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ResolvingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CircularProgressIndicator(modifier = Modifier.size(72.dp))
        Text(
            text = stringResource(R.string.scan_dialog_body_resolving),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ResultContent(result: AccessResult, zoneId: ZoneId) {
    val granted = result.decision.isAllowed
    val accent = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = if (granted) Icons.Filled.CheckCircle else Icons.Filled.Block,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(96.dp),
        )
        // offline 판정은 master 캐시에 PII 가 없어 name 이 빈 문자열 — employeeCode 로 표시.
        // online 응답은 server 가 보내준 name 을 그대로 노출. (hybrid-offline.md §2.1 의 "B-pragmatic":
        // 디스크에는 안 저장, server 응답의 일시적 사용은 OK.)
        if (result.user.name.isNotBlank()) {
            Text(
                text = result.user.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.scan_result_employee_code, result.user.employeeCode.value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = stringResource(R.string.scan_result_employee_code, result.user.employeeCode.value),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (!granted) {
            Text(
                text = result.denyReason
                    ?.toKoreanLabelRes()
                    ?.let { stringResource(it) }
                    ?: stringResource(R.string.scan_result_deny_reason_unknown),
                style = MaterialTheme.typography.bodyMedium,
                color = accent,
                textAlign = TextAlign.Center,
            )
        } else {
            // 허용 + validUntil non-null 인 경우만 노출. null 은 무기한 권한이라 표시 생략.
            // 표시 zone 은 TimeProvider.zoneId() 에서 주입 (architecture.md §7) — ScanUiState 경유.
            result.validUntil?.let { until ->
                Text(
                    text = stringResource(
                        R.string.scan_result_valid_until,
                        AppDateTimeFormatter.date(until, zoneId),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(uiError: UiError) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(72.dp),
        )
        Text(
            text = uiError.message.asString(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Unspecified,
        )
    }
}
