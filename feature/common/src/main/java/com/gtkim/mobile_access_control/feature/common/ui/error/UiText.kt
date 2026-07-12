package com.gtkim.mobile_access_control.feature.common.ui.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gtkim.mobile_access_control.core.common.error.UiText

/**
 * [UiText] 의 Compose 해석 진입점. 본 확장은 @Composable 이므로 :feature 레이어에 둔다 —
 * UiText sealed 타입은 :core 에 위치 (도메인 에러도 사용).
 */
@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Res -> stringResource(id)
    is UiText.FormattedRes -> stringResource(id, *args.toTypedArray())
    is UiText.Raw -> value
}
