package com.gtkim.mobile_access_control.feature.common.ui.scaffold

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gtkim.mobile_access_control.feature.common.R

/**
 * 좌측 햄버거 + 가운데 정렬 제목 + 우측 actions 의 상단 바. stateless — 제목, 메뉴 콜백, 우측 actions
 * 슬롯만 받는다. 메인 셸 안에서만 쓰이며 항상 노출되므로 표시 여부 분기는 두지 않는다.
 *
 * actions 슬롯은 큐 동기화 / 오프라인 배지처럼 화면별 컨텍스트가 아닌 단말 전역 상태를 위한 자리.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.topbar_open_menu),
                )
            }
        },
        actions = actions,
        modifier = modifier,
    )
}
