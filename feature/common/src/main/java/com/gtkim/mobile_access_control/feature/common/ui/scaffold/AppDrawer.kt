package com.gtkim.mobile_access_control.feature.common.ui.scaffold

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gtkim.mobile_access_control.feature.common.R

private val BOTTOM_GAP = 24.dp

/**
 * 햄버거 메뉴 본체. 라우팅을 모르는 stateless 컴포저블 — destination 목록과 선택 콜백,
 * 그리고 인증과 분리된 로그아웃 콜백만 받는다.
 *
 * 로그아웃 항목은 destinations 와 시각적으로 분리하기 위해 [Spacer]+[HorizontalDivider] 로
 * 떨어뜨리고, 화면 하단에 살짝 떠 있도록 [BOTTOM_GAP] 만큼 여유를 둔다.
 */
@Composable
fun AppDrawer(
    destinations: List<AppDrawerDestination>,
    selectedKey: String?,
    onSelect: (AppDrawerDestination) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(modifier = modifier.width(280.dp)) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.drawer_header),
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(16.dp))
        destinations.forEach { dest ->
            NavigationDrawerItem(
                icon = { Icon(dest.icon, contentDescription = null) },
                label = { Text(dest.label) },
                selected = dest.key == selectedKey,
                onClick = { onSelect(dest) },
                // 기본 selectedContainerColor(secondaryContainer) 가 이 모노톤 팔레트에선 드로어
                // 표면(near-white)과 거의 동색이라 선택 pill 이 안 보인다 → surfaceContainerHighest 로 대비 확보.
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
        }
        // weight(1f) 로 남는 공간을 차지해 로그아웃 항목을 하단으로 밀어낸다.
        Spacer(Modifier.weight(1f))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 28.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Spacer(Modifier.height(8.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
            label = { Text(stringResource(R.string.drawer_logout)) },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
        Spacer(Modifier.height(BOTTOM_GAP))
    }
}
