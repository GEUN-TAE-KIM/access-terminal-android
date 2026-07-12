package com.gtkim.mobile_access_control.feature.history.mapper

import com.gtkim.mobile_access_control.component.history.domain.model.HistoryError
import com.gtkim.mobile_access_control.core.common.result.AppError
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.feature.history.R
import com.gtkim.mobile_access_control.feature.common.R as CommonR

/**
 * History 화면용 도메인 에러 → [UiError] 매퍼 (presentation 레이어 책임).
 *
 * [HistoryError] sealed 의 각 case 별 title/message/action 을 명시 — exhaustive `when` 으로 새 case
 * 추가 시 컴파일 에러로 잡힌다. catch-all `else` 금지 (architecture.md §16).
 */
internal fun AppError.toUiError(): UiError = when (this) {
    is HistoryError -> toUiError()
    else -> UiError(
        title = UiText.Res(CommonR.string.error_title_generic),
        message = UiText.Res(CommonR.string.error_message_unknown),
    )
}

private fun HistoryError.toUiError(): UiError = when (this) {
    is HistoryError.ValidationFailed -> UiError(
        title = UiText.Res(CommonR.string.error_title_validation_query),
        message = message,
    )
    is HistoryError.SessionExpired -> UiError(
        title = UiText.Res(CommonR.string.error_title_session_expired),
        message = message,
        confirmText = UiText.Res(CommonR.string.error_button_reauthenticate),
        action = UiError.Action.Reauthenticate,
    )
    is HistoryError.Forbidden -> UiError(
        title = UiText.Res(CommonR.string.error_title_forbidden),
        message = message,
    )
    is HistoryError.NoConnection -> UiError(
        title = UiText.Res(CommonR.string.error_title_no_connection),
        message = message,
    )
    is HistoryError.Timeout -> UiError(
        title = UiText.Res(CommonR.string.error_title_timeout),
        message = message,
    )
    is HistoryError.ServerError -> UiError(
        title = UiText.Res(CommonR.string.error_title_server),
        message = message,
    )
    is HistoryError.Unknown -> UiError(
        title = UiText.Res(R.string.history_error_unknown_title),
        message = message,
    )
}
