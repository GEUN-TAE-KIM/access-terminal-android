package com.gtkim.mobile_access_control.feature.common.ui.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gtkim.mobile_access_control.feature.common.R

/**
 * [AppTopBar] 의 actions 슬롯에 들어가는 단말 전역 상태 표시 조각들.
 *
 * 라우트/Intent 를 모르는 stateless 디자인 컴포넌트 — 상태는 prop 으로 받고 액션은 콜백으로 위임한다.
 * 어느 화면에서 어떤 Intent 를 발사할지(조립)는 :app 의 AppShell 이 책임진다. [AppTopBar]/[AppDrawer]
 * 와 같은 scaffold 패키지에 두어 셸 디자인 조각을 한 곳에 모은다.
 */

/**
 * 오프라인 상태일 때만 노출되는 작은 배지 — errorContainer 색으로 시각화. 모든 화면 공통 노출이라
 * 운영자가 다른 화면에 있어도 단말이 오프라인인 걸 즉시 인지할 수 있다.
 */
@Composable
fun OfflineBadge(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.topbar_offline_badge),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onErrorContainer,
        modifier = modifier
            .padding(end = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

/**
 * 미전송 큐가 쌓였을 때만 노출되는 경고성 액션 — BadgedBox 로 큐 카운트 표시. 클릭은 콜백으로 위임한다.
 * 호출 측이 pendingCount > 0 일 때만 노출하므로 빈 큐 분기는 두지 않는다.
 */
@Composable
fun SyncAction(
    pendingCount: Int,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        BadgedBox(badge = { Badge { Text(pendingCount.toString()) } }) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = stringResource(R.string.topbar_sync_action),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
