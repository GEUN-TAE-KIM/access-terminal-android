package com.gtkim.mobile_access_control.feature.stats.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gtkim.mobile_access_control.component.stats.domain.model.DailyStats
import com.gtkim.mobile_access_control.component.stats.domain.model.DenyReasonCount
import com.gtkim.mobile_access_control.component.stats.domain.model.HourlyCount
import com.gtkim.mobile_access_control.component.stats.domain.model.StatsSummary
import com.gtkim.mobile_access_control.core.model.DenyReason
import com.gtkim.mobile_access_control.feature.common.ui.component.AppDatePickerDialog
import com.gtkim.mobile_access_control.feature.common.ui.component.AppLoadingIndicator
import com.gtkim.mobile_access_control.feature.common.ui.dialog.AppDialog
import com.gtkim.mobile_access_control.feature.stats.ui.component.StatsBody
import com.gtkim.mobile_access_control.feature.stats.ui.component.StatsHeader
import com.gtkim.mobile_access_control.feature.common.ui.theme.AppTheme
import org.orbitmvi.orbit.compose.collectAsState
import java.time.LocalDate

@Composable
fun StatsScreen() {
    val viewModel: StatsViewModel = hiltViewModel()
    val state by viewModel.collectAsState()

    StatsScaffold(state = state, onIntent = viewModel::onIntent)
}

@Composable
internal fun StatsScaffold(
    state: StatsUiState,
    onIntent: (StatsIntent) -> Unit,
) {
    // DatePicker 노출 여부는 화면 한정 일시 UI 상태 — 선택 결과만 StatsIntent.DateSelected 로
    // ViewModel 에 전달된다. rememberSaveable 로 구성 변경(회전 등) 시에도 열림 상태 보존.
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    // 로딩 중에는 헤더까지 가리고 화면 정중앙에 통일된 AppLoadingIndicator 만 표시
    // (History 화면과 동일). 데이터/Empty 상태일 때만 헤더 + 본문 레이아웃을 그린다.
    if (state.loading) {
        AppLoadingIndicator(modifier = Modifier.fillMaxSize())
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatsHeader(
                date = state.date,
                onDateClick = { showDatePicker = true },
                onRefresh = { onIntent(StatsIntent.Refresh) },
            )
            StatsBody(
                stats = state.stats,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }

    if (showDatePicker) {
        // 다이얼로그를 열 때마다 새로 구성 → rememberDatePickerState 가 현재 state.date 로 매번 초기화.
        AppDatePickerDialog(
            selectedDate = state.date,
            onDismiss = { showDatePicker = false },
            onDateSelected = { date ->
                showDatePicker = false
                onIntent(StatsIntent.DateSelected(date))
            },
        )
    }

    state.dialog?.let { dialog ->
        AppDialog(
            dialog = dialog,
            onDismiss = { onIntent(StatsIntent.DialogDismissed) },
            onConfirm = { onIntent(StatsIntent.DialogConfirmed) },
        )
    }
}

@Preview
@Composable
private fun StatsScaffoldPreview() {
    AppTheme {
        StatsScaffold(
            state = StatsUiState(
                date = LocalDate.of(2026, 5, 29),
                stats = DailyStats(
                    date = LocalDate.of(2026, 5, 29),
                    summary = StatsSummary(totalAttempts = 142, allowed = 130, denied = 12, allowedRate = 0.915),
                    byHour = (0..23).map { HourlyCount(it, if (it in 9..18) it - 5 else 0, if (it % 5 == 0) 1 else 0) },
                    byDenyReason = listOf(
                        DenyReasonCount(DenyReason.NO_PERMISSION_FOR_ZONE, 6),
                        DenyReasonCount(DenyReason.OUT_OF_ALLOWED_HOURS, 4),
                        DenyReasonCount(DenyReason.USER_INACTIVE, 2),
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}
