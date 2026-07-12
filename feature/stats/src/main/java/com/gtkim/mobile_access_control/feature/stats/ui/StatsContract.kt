package com.gtkim.mobile_access_control.feature.stats.ui

import com.gtkim.mobile_access_control.component.stats.domain.model.DailyStats
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import java.time.LocalDate

internal data class StatsUiState(
    val date: LocalDate,
    val loading: Boolean = false,
    val stats: DailyStats? = null,
    val dialog: DialogState? = null,
)

internal sealed interface StatsIntent {
    data class DateSelected(
        val date: LocalDate,
    ) : StatsIntent
    data object Refresh : StatsIntent
    data object DialogDismissed : StatsIntent
    data object DialogConfirmed : StatsIntent
}
