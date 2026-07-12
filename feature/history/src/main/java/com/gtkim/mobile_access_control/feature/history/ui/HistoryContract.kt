package com.gtkim.mobile_access_control.feature.history.ui

import com.gtkim.mobile_access_control.component.history.domain.model.AccessLog
import com.gtkim.mobile_access_control.component.history.domain.model.LogCursor
import com.gtkim.mobile_access_control.component.history.domain.model.LogFilter
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import java.time.ZoneId

internal data class HistoryUiState(
    /** 로그 시각 표시용 타임존(JST). ViewModel 이 TimeProvider.zoneId() 로 1회 주입 — architecture.md §7. */
    val zoneId: ZoneId,
    val items: List<AccessLog> = emptyList(),
    val filter: LogFilter = LogFilter(),
    val nextCursor: LogCursor? = null,
    val loadingPage: Boolean = false,
    val isRefreshing: Boolean = false,
    val endReached: Boolean = false,
    val dialog: DialogState? = null,
)

internal sealed interface HistoryIntent {
    data object LoadNext : HistoryIntent
    data class FilterChanged(val filter: LogFilter) : HistoryIntent
    data object Refresh : HistoryIntent
    data object DialogDismissed : HistoryIntent
    data object DialogConfirmed : HistoryIntent
}
