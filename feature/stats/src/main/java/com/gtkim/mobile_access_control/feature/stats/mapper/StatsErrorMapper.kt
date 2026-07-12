package com.gtkim.mobile_access_control.feature.stats.mapper

import com.gtkim.mobile_access_control.component.stats.domain.model.StatsError
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.core.common.result.AppError
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.feature.stats.R
import com.gtkim.mobile_access_control.feature.common.R as CommonR

/**
 * Stats 화면용 도메인 에러 → [UiError] 매퍼 (presentation 레이어 책임).
 *
 * [StatsError] sealed 의 각 case 별 title/message/action 을 명시 — exhaustive `when` 으로 새 case
 * 추가 시 컴파일 에러로 잡힌다. catch-all `else` 금지 (architecture.md §16).
 */
internal fun AppError.toUiError(): UiError = when (this) {
    is StatsError -> toUiError()
    else -> UiError(
        title = UiText.Res(CommonR.string.error_title_generic),
        message = UiText.Res(CommonR.string.error_message_unknown),
    )
}

private fun StatsError.toUiError(): UiError = when (this) {
    is StatsError.ValidationFailed -> UiError(
        title = UiText.Res(CommonR.string.error_title_validation_query),
        message = message,
    )

    is StatsError.SessionExpired -> UiError(
        title = UiText.Res(CommonR.string.error_title_session_expired),
        message = message,
        confirmText = UiText.Res(CommonR.string.error_button_reauthenticate),
        action = UiError.Action.Reauthenticate,
    )

    is StatsError.Forbidden -> UiError(
        title = UiText.Res(CommonR.string.error_title_forbidden),
        message = message,
    )

    is StatsError.NoConnection -> UiError(
        title = UiText.Res(CommonR.string.error_title_no_connection),
        message = message,
    )

    is StatsError.Timeout -> UiError(
        title = UiText.Res(CommonR.string.error_title_timeout),
        message = message,
    )

    is StatsError.ServerError -> UiError(
        title = UiText.Res(CommonR.string.error_title_server),
        message = message,
    )

    is StatsError.Unknown -> UiError(
        title = UiText.Res(R.string.stats_error_unknown_title),
        message = message,
    )
}
