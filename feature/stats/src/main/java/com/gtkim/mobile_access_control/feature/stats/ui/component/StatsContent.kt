package com.gtkim.mobile_access_control.feature.stats.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gtkim.mobile_access_control.component.stats.domain.model.DailyStats
import com.gtkim.mobile_access_control.feature.common.ui.component.AppButton
import com.gtkim.mobile_access_control.feature.stats.R
import java.time.LocalDate

@Composable
internal fun StatsHeader(
    date: LocalDate,
    onDateClick: () -> Unit,
    onRefresh: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = date.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppButton(
                text = stringResource(R.string.stats_button_change_date),
                onClick = onDateClick,
                modifier = Modifier.weight(1f),
            )
            AppButton(
                text = stringResource(R.string.stats_button_refresh),
                onClick = onRefresh,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun StatsBody(
    stats: DailyStats?,
    modifier: Modifier = Modifier,
) {
    when {
        stats == null -> EmptyState(
            modifier = modifier,
            message = stringResource(R.string.stats_empty_load_failed),
        )

        stats.summary.totalAttempts == 0 -> EmptyState(
            modifier = modifier,
            message = stringResource(R.string.stats_empty_no_attempts),
        )

        else -> StatsContent(stats = stats, modifier = modifier)
    }
}

@Composable
private fun StatsContent(stats: DailyStats, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SummaryKpiGrid(summary = stats.summary)

        SectionTitle(stringResource(R.string.stats_section_hourly))
        HourlyBarChart(data = stats.byHour)

        if (stats.byDenyReason.isNotEmpty()) {
            SectionTitle(stringResource(R.string.stats_section_deny_reason))
            DenyReasonBreakdown(items = stats.byDenyReason)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Composable
private fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
