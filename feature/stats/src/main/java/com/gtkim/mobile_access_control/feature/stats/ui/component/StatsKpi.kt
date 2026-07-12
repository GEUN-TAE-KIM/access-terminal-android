package com.gtkim.mobile_access_control.feature.stats.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gtkim.mobile_access_control.component.stats.domain.model.DenyReasonCount
import com.gtkim.mobile_access_control.component.stats.domain.model.StatsSummary
import com.gtkim.mobile_access_control.feature.common.ui.label.toKoreanLabelRes
import com.gtkim.mobile_access_control.feature.stats.R
import com.gtkim.mobile_access_control.feature.common.R as CommonR

/**
 * 상단 KPI 4종 카드 (총 시도 / 허용 / 거부 / 허용률).
 *
 * 허용률은 [StatsSummary.allowedRate] 가 `null` 이면 "-" 로 표시 (API 명세 §6.1 의
 * "totalAttempts=0 시 null" 정책과 정합).
 */
@Composable
internal fun SummaryKpiGrid(
    summary: StatsSummary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                title = stringResource(R.string.stats_kpi_total),
                value = summary.totalAttempts.toString(),
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            KpiCard(
                title = stringResource(CommonR.string.common_allowed),
                value = summary.allowed.toString(),
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                title = stringResource(CommonR.string.common_denied),
                value = summary.denied.toString(),
                accent = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
            KpiCard(
                title = stringResource(R.string.stats_kpi_allowed_rate),
                value = summary.allowedRate?.let { "%.1f%%".format(it * 100) } ?: "-",
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = accent,
            )
        }
    }
}

/**
 * 거부 사유별 카운트 시각화. 항목별 가로 progress 바 — 최대 count 대비 비율.
 *
 * 서버가 count 내림차순으로 정렬해 보내므로 (API 명세 §6.1) 클라는 그대로 표시한다.
 * 거부가 한 건도 없으면 빈 리스트가 오므로 호출자가 노출 여부를 제어한다.
 */
@Composable
internal fun DenyReasonBreakdown(
    items: List<DenyReasonCount>,
    modifier: Modifier = Modifier,
) {
    val maxCount = (items.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items.forEach { item ->
            // wire 변환은 data 레이어 책임 — 여기선 typed enum 만 다룬다. UNKNOWN 도 한국어 fallback 보장.
            val label = stringResource(item.reason.toKoreanLabelRes())
            DenyReasonRow(
                label = label,
                count = item.count,
                ratio = item.count.toFloat() / maxCount,
            )
        }
    }
}

@Composable
private fun DenyReasonRow(label: String, count: Int, ratio: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.stats_deny_count, count),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.error,
            )
        }
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(top = 4.dp),
            color = MaterialTheme.colorScheme.error,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
