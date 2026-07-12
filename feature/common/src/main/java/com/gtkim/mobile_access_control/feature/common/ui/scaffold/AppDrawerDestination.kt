package com.gtkim.mobile_access_control.feature.common.ui.scaffold

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 햄버거 메뉴 항목의 외관 데이터. Navigation 라우트 객체는 :app 모듈에 있으므로 본 데이터는
 * 라우트를 모르고 식별자(key)만 들고 있다 — 라우트 매핑은 [com.gtkim.mobile_access_control.navigation.AppShell]
 * 이 담당해 :feature:common → :app 역의존을 피한다.
 */
data class AppDrawerDestination(
    val key: String,
    val label: String,
    val icon: ImageVector,
)
