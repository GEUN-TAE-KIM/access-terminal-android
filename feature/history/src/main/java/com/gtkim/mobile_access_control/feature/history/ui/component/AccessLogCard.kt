package com.gtkim.mobile_access_control.feature.history.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gtkim.mobile_access_control.component.history.domain.model.AccessLog
import com.gtkim.mobile_access_control.component.history.domain.model.LogResult
import com.gtkim.mobile_access_control.core.common.time.AppDateTimeFormatter
import com.gtkim.mobile_access_control.feature.common.ui.label.toKoreanLabelRes
import com.gtkim.mobile_access_control.feature.history.R
import java.time.ZoneId
import com.gtkim.mobile_access_control.feature.common.R as CommonR

/**
 * 출입 기록 한 건을 카드로 표시 (roadmap.md §2 "히스토리 필터 UI" 후속 — 리스트 가독성 개선).
 *
 * 한 줄 텍스트 대신 시각·결과 배지·사용자/UID·구역·거부 사유를 묶어 보여준다. 색상 톤은 결과 배지에만
 * 적용해 정보 밀도가 높은 화면에서 시각적 노이즈를 최소화한다.
 */
@Composable
internal fun AccessLogCard(
    log: AccessLog,
    zoneId: ZoneId,
    modifier: Modifier = Modifier,
) {
    // UTC Instant → 단말 타임존 문자열. recomposition 마다 재계산하지 않도록 캐시 (architecture.md §7).
    val time = remember(log.id, zoneId) {
        AppDateTimeFormatter.dateTime(log.attemptedAt, zoneId)
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = time, style = MaterialTheme.typography.titleSmall)
                ResultBadge(result = log.result)
            }

            // 사용자명 + 사번 — 미등록 카드(user=null)는 안내 문구로 대체.
            // stringResource 는 @Composable 이라 ?.let 의 (non-inline) 람다에서 호출이 모호 — if 로 명시.
            val user = log.user
            val userLine = if (user != null) {
                stringResource(R.string.history_card_user_line, user.name, user.employeeCode.value)
            } else {
                stringResource(R.string.history_card_unregistered)
            }
            Text(text = userLine, style = MaterialTheme.typography.bodyMedium)

            Text(
                text = stringResource(
                    R.string.history_card_zone_uid,
                    log.zone.value,
                    log.cardUid.value
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // 거부 사유 — 허용일 땐 null 이라 자동 생략 (?.let).
            log.denyReason?.let { reason ->
                Text(
                    text = stringResource(
                        R.string.history_card_reason_prefix,
                        stringResource(reason.toKoreanLabelRes()),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ResultBadge(result: LogResult) {
    val tint = when (result) {
        LogResult.ALLOWED -> MaterialTheme.colorScheme.primary
        LogResult.DENIED_NO_PERMISSION,
        LogResult.DENIED_OUT_OF_HOURS,
        LogResult.DENIED_EXPIRED,
        LogResult.DENIED_INACTIVE_CARD,
        LogResult.DENIED_INACTIVE_USER -> MaterialTheme.colorScheme.error

        LogResult.UNKNOWN -> MaterialTheme.colorScheme.outline
    }
    Text(
        text = stringResource(result.labelRes()),
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        modifier = Modifier
            .background(color = tint, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@StringRes
private fun LogResult.labelRes(): Int = when (this) {
    LogResult.ALLOWED -> CommonR.string.common_allowed
    LogResult.DENIED_NO_PERMISSION,
    LogResult.DENIED_OUT_OF_HOURS,
    LogResult.DENIED_EXPIRED,
    LogResult.DENIED_INACTIVE_CARD,
    LogResult.DENIED_INACTIVE_USER -> CommonR.string.common_denied

    LogResult.UNKNOWN -> CommonR.string.common_unknown
}
