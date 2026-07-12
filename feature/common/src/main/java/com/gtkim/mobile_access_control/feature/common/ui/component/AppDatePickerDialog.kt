package com.gtkim.mobile_access_control.feature.common.ui.component

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gtkim.mobile_access_control.feature.common.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Material3 DatePickerDialog 공통 래퍼 (History·Stats 공용).
 *
 * DatePicker 는 epoch millis(UTC 자정 기준) 로만 날짜를 다루므로, LocalDate ↔ millis 변환을
 * **UTC 로 통일**해 한 곳에서 처리한다 — 타임존을 섞으면 하루 밀리는 off-by-one 위험. 호출 측은
 * 순수 "달력 날짜"([LocalDate]) 만 주고받고, 화면별 타임존/Instant 변환은 호출 측 경계에서 한다
 * (Stats: 그대로 사용 / History: 자기 zoneId 로 Instant ↔ LocalDate 변환).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    selectedDate: LocalDate?,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            ?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        onDateSelected(
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate(),
                        )
                    } else {
                        onDismiss()
                    }
                },
            ) { Text(stringResource(R.string.common_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
