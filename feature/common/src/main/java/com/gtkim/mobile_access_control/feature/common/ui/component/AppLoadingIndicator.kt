package com.gtkim.mobile_access_control.feature.common.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 공용 로딩 인디케이터 — 전달된 영역의 가운데에 CircularProgressIndicator 1개를 띄운다.
 * 전체화면 로딩 / 리스트 페이지네이션 인라인 로딩 모두 호출 측이 modifier 로 사이즈를 결정한다.
 */
@Composable
fun AppLoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
