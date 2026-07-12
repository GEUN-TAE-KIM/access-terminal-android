package com.gtkim.mobile_access_control.feature.stats.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gtkim.mobile_access_control.component.stats.domain.model.HourlyCount
import com.gtkim.mobile_access_control.feature.stats.R
import com.gtkim.mobile_access_control.feature.common.R as CommonR

private const val HOURS_PER_DAY = 24
private const val BAR_RATIO = 0.6f
private const val AXIS_LABEL_AREA_PX = 24f

/**
 * 시간대별 출입 stacked bar chart (0~23시).
 *
 * - 막대 아래쪽 = 허용(primary), 위쪽 = 거부(error) — 시각적으로 "안전한" 색이 토대
 * - X축 라벨은 6시간 간격(0/6/12/18)만 노출해 가독성 확보
 * - 모든 시간대가 0이면 height 가 0 인 막대 대신 baseline 만 표시 (max 값 fallback = 1)
 * - 라이브러리 없이 [Canvas] 만으로 그린다 — Vico/MPAndroidChart 의 transitive 의존을 피함
 */
@Composable
internal fun HourlyBarChart(
    data: List<HourlyCount>,
    modifier: Modifier = Modifier,
) {
    val maxValue = (data.maxOfOrNull { it.allowed + it.denied } ?: 0).coerceAtLeast(1)
    val allowedColor = MaterialTheme.colorScheme.primary
    val deniedColor = MaterialTheme.colorScheme.error
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val axisLabelStyle = TextStyle(
        color = axisLabelColor,
        fontSize = 11.sp,
        textAlign = TextAlign.Center,
    )

    // X축 라벨은 Canvas 의 DrawScope 안에서 그려지지만 그쪽은 @Composable 이 아니라 stringResource
    // 를 직접 호출할 수 없다 — 본 @Composable 스코프에서 미리 해석한 뒤 클로저로 캡처.
    val hourLabels: List<Pair<Int, String>> = listOf(0, 6, 12, 18).map { hour ->
        hour to stringResource(R.string.stats_chart_hour_label, hour)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChartLegend(color = allowedColor, label = stringResource(CommonR.string.common_allowed))
            Spacer(modifier = Modifier.width(16.dp))
            ChartLegend(color = deniedColor, label = stringResource(CommonR.string.common_denied))
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.stats_chart_max, maxValue),
                style = MaterialTheme.typography.labelSmall,
                color = axisLabelColor,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        ) {
            val chartHeight = size.height - AXIS_LABEL_AREA_PX
            val barAreaWidth = size.width
            val slotWidth = barAreaWidth / HOURS_PER_DAY
            val barWidth = slotWidth * BAR_RATIO

            // 베이스라인 (X축).
            drawLine(
                color = gridColor,
                start = Offset(0f, chartHeight),
                end = Offset(size.width, chartHeight),
                strokeWidth = Stroke.HairlineWidth,
            )

            data.forEach { bucket ->
                val slotLeft = slotWidth * bucket.hour
                val barLeft = slotLeft + (slotWidth - barWidth) / 2f
                val allowedHeight = chartHeight * (bucket.allowed.toFloat() / maxValue)
                val deniedHeight = chartHeight * (bucket.denied.toFloat() / maxValue)

                if (bucket.allowed > 0) {
                    drawRect(
                        color = allowedColor,
                        topLeft = Offset(barLeft, chartHeight - allowedHeight),
                        size = Size(barWidth, allowedHeight),
                    )
                }
                if (bucket.denied > 0) {
                    drawRect(
                        color = deniedColor,
                        topLeft = Offset(barLeft, chartHeight - allowedHeight - deniedHeight),
                        size = Size(barWidth, deniedHeight),
                    )
                }
            }

            // X축 라벨 — 0/6/12/18 만. 라벨 문자열은 위 Composable 스코프에서 미리 해석된 것을 사용.
            hourLabels.forEach { (hour, label) ->
                val slotCenterX = slotWidth * hour + slotWidth / 2f
                val layout = textMeasurer.measure(
                    text = label,
                    style = axisLabelStyle,
                )
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        x = slotCenterX - layout.size.width / 2f,
                        y = chartHeight + 4f,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ChartLegend(color: Color, label: String) {
    // Box.background 직접 그림 — 이전 구현은 Canvas 가 height 미지정으로 0 픽셀이라 색이
    // 실제로 안 보였다. 라벨 옆 색 박스가 차트의 막대 색과 1:1 매칭되어 허용/거부 구분에 사용됨.
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = color, shape = RoundedCornerShape(2.dp)),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
