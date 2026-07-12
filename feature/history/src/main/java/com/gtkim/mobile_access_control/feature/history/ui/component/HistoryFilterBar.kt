package com.gtkim.mobile_access_control.feature.history.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.gtkim.mobile_access_control.component.history.domain.model.LogFilter
import com.gtkim.mobile_access_control.component.history.domain.model.LogResultFilter
import com.gtkim.mobile_access_control.core.common.time.AppDateTimeFormatter
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.DenyReason
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import com.gtkim.mobile_access_control.feature.common.ui.component.AppDatePickerDialog
import com.gtkim.mobile_access_control.feature.common.ui.label.toKoreanLabelRes
import com.gtkim.mobile_access_control.feature.history.R
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import com.gtkim.mobile_access_control.feature.common.R as CommonR

/**
 * 출입 기록 조회 필터바 (roadmap.md §2 "히스토리 필터 UI").
 *
 * - 결과 칩(ALL/ALLOWED/DENIED) 은 클릭 즉시 발행.
 * - 거부 사유 드롭다운은 result 가 ALLOWED 가 아닐 때만 노출 — ALLOWED 로 전환되면 자동으로 비운다.
 * - 사번/UID 텍스트는 로컬 편집 버퍼(`empBuffer`/`uidBuffer`)에 두고 IME Done 시 발행 —
 *   매 키 입력마다 페이지네이션 리셋되는 것 방지. 외부에서 reset 되면(`null`) 버퍼도 비운다.
 * - 기간은 JST([zoneId]) 기준 `from` = 시작일 00:00, `to` = 종료일 23:59:59.999.
 * - "초기화" 버튼은 [LogFilter] default 로 복귀 (외부 null sync 가 텍스트 버퍼도 비운다).
 *
 * 클라 측 필터링 금지 — 모든 필터는 서버 쿼리 파라미터로 전달된다 (architecture.md §2 Cursor Pagination).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryFilterBar(
    filter: LogFilter,
    zoneId: ZoneId,
    onFilterChange: (LogFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ResultChipsRow(
            current = filter.result,
            onSelect = { selected ->
                // ALLOWED 로 전환되면 denyReason 은 의미가 없으므로 함께 비운다 (서버 결과가 항상 0건이 됨).
                val nextDenyReason =
                    filter.denyReason.takeIf { selected != LogResultFilter.ALLOWED }
                onFilterChange(filter.copy(result = selected, denyReason = nextDenyReason))
            },
            onReset = { onFilterChange(LogFilter()) },
        )
        // result=ALLOWED 에서는 denyReason 필터가 무의미하므로 숨긴다.
        if (filter.result != LogResultFilter.ALLOWED) {
            DenyReasonDropdown(
                current = filter.denyReason,
                onSelect = { onFilterChange(filter.copy(denyReason = it)) },
            )
        }
        TextFiltersRow(
            employeeCode = filter.employeeCode,
            cardUid = filter.cardUid,
            onEmployeeCommit = { trimmed ->
                onFilterChange(filter.copy(employeeCode = trimmed?.let(::EmployeeCode)))
            },
            onCardUidCommit = { trimmed ->
                onFilterChange(filter.copy(cardUid = trimmed?.let(::CardUid)))
            },
        )
        DateRangeRow(
            from = filter.from,
            to = filter.to,
            zoneId = zoneId,
            onFromChange = { onFilterChange(filter.copy(from = it)) },
            onToChange = { onFilterChange(filter.copy(to = it)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultChipsRow(
    current: LogResultFilter,
    onSelect: (LogResultFilter) -> Unit,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            LogResultFilter.entries.forEach { option ->
                val isSelected = current == option
                // Material3 기본은 미선택 칩에 outline 을 주는데, 본 화면은 그 의미를 반전한다 —
                // 선택된 칩만 primary 색 outline + 텍스트로 강조하고 미선택은 보더 없이 둔다.
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(option) },
                    label = { Text(stringResource(option.labelRes())) },
                    border = if (isSelected) {
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        null
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.Transparent,
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        }
        TextButton(onClick = onReset) {
            Text(stringResource(CommonR.string.common_reset))
        }
    }
}

@androidx.annotation.StringRes
private fun LogResultFilter.labelRes(): Int = when (this) {
    LogResultFilter.ALL -> R.string.history_filter_all
    LogResultFilter.ALLOWED -> CommonR.string.common_allowed
    LogResultFilter.DENIED -> CommonR.string.common_denied
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DenyReasonDropdown(
    current: DenyReason?,
    onSelect: (DenyReason?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = current?.toKoreanLabelRes()
        ?.let { stringResource(it) }
        ?: stringResource(R.string.history_filter_deny_reason_all)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.history_filter_deny_reason_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
        )
        // ExposedDropdownMenu (scope 멤버) 는 anchor (위 OutlinedTextField) 너비에 자동 매칭된다.
        // 일반 DropdownMenu 는 항목 wrap-content 라 anchor 너비를 따르지 않는다.
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.history_filter_deny_reason_all)) },
                onClick = {
                    expanded = false
                    onSelect(null)
                },
            )
            DenyReason.selectable.forEach { reason ->
                DropdownMenuItem(
                    text = { Text(stringResource(reason.toKoreanLabelRes())) },
                    onClick = {
                        expanded = false
                        onSelect(reason)
                    },
                )
            }
        }
    }
}

@Composable
private fun TextFiltersRow(
    employeeCode: EmployeeCode?,
    cardUid: CardUid?,
    onEmployeeCommit: (String?) -> Unit,
    onCardUidCommit: (String?) -> Unit,
) {
    // 편집 중에는 외부 filter 값을 덮어쓰지 않되, 외부 reset(`null` 진입) 시에만 동기화한다.
    // rememberSaveable 로 회전 등 구성 변경에도 편집 중 입력을 보존.
    var empBuffer by rememberSaveable { mutableStateOf(employeeCode?.value.orEmpty()) }
    var uidBuffer by rememberSaveable { mutableStateOf(cardUid?.value.orEmpty()) }

    LaunchedEffect(employeeCode) {
        if (employeeCode == null) empBuffer = ""
    }
    LaunchedEffect(cardUid) {
        if (cardUid == null) uidBuffer = ""
    }

    val partialHint = stringResource(R.string.history_filter_partial_match_hint)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = empBuffer,
            onValueChange = { empBuffer = it },
            label = { Text(stringResource(R.string.history_filter_employee_code)) },
            supportingText = { Text(partialHint) },
            singleLine = true,
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onEmployeeCommit(empBuffer.trim().takeIf { it.isNotEmpty() }) },
            ),
        )
        OutlinedTextField(
            value = uidBuffer,
            onValueChange = { uidBuffer = it },
            label = { Text(stringResource(R.string.history_filter_card_uid)) },
            supportingText = { Text(partialHint) },
            singleLine = true,
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onCardUidCommit(uidBuffer.trim().takeIf { it.isNotEmpty() }) },
            ),
        )
    }
}

@Composable
private fun DateRangeRow(
    from: Instant?,
    to: Instant?,
    zoneId: ZoneId,
    onFromChange: (Instant?) -> Unit,
    onToChange: (Instant?) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        DatePickerField(
            label = stringResource(R.string.history_filter_date_from),
            value = from,
            zoneId = zoneId,
            modifier = Modifier.weight(1f),
        ) { picked ->
            // 시작일 = 그 날의 JST 00:00.
            onFromChange(picked?.atStartOfDay(zoneId)?.toInstant())
        }
        DatePickerField(
            label = stringResource(R.string.history_filter_date_to),
            value = to,
            zoneId = zoneId,
            modifier = Modifier.weight(1f),
        ) { picked ->
            // 종료일 = 그 날의 JST 23:59:59.999 (LocalTime.MAX).
            onToChange(picked?.atTime(LocalTime.MAX)?.atZone(zoneId)?.toInstant())
        }
    }
}

@Composable
private fun DatePickerField(
    label: String,
    value: Instant?,
    zoneId: ZoneId,
    modifier: Modifier = Modifier,
    onPicked: (java.time.LocalDate?) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val display = value?.let { AppDateTimeFormatter.date(it, zoneId) }.orEmpty()

    OutlinedTextField(
        value = display,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        singleLine = true,
        modifier = modifier,
        trailingIcon = {
            TextButton(onClick = { showDialog = true }) {
                Text(stringResource(CommonR.string.common_select))
            }
        },
    )

    if (showDialog) {
        // 저장된 Instant 를 JST 달력 날짜로 변환해 picker 초기값으로 넘긴다. 선택 결과(LocalDate)는
        // 호출 측(DateRangeRow)이 from=00:00 / to=23:59:59.999 로 다시 Instant 화한다.
        AppDatePickerDialog(
            selectedDate = value?.atZone(zoneId)?.toLocalDate(),
            onDismiss = { showDialog = false },
            onDateSelected = { picked ->
                onPicked(picked)
                showDialog = false
            },
        )
    }
}
