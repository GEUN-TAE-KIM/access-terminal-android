package com.gtkim.mobile_access_control.feature.login.mapper

import com.gtkim.mobile_access_control.component.auth.domain.model.AuthError
import com.gtkim.mobile_access_control.core.common.result.AppError
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.feature.login.R
import com.gtkim.mobile_access_control.feature.common.R as CommonR

/**
 * Login 화면용 도메인 에러 → [UiError] 매퍼 (presentation 레이어 책임).
 *
 * [AuthError] sealed 의 각 case 별 title/message/action 을 명시 — exhaustive `when` 으로 새 case
 * 추가 시 컴파일 에러로 잡힌다. catch-all `else` 금지 (architecture.md §16).
 *
 * 로그인 화면 한정 특수성: 이미 LoginRoute 에 있으므로 [UiError.Action.Reauthenticate] 를 부여하지
 * 않는다 — SessionExpired 가 들어와도 다시 로그인 폼만 보여주면 된다.
 */
internal fun AppError.toUiError(): UiError = when (this) {
    is AuthError -> toUiError()
    // AppError 는 멀티모듈 경계 너머라 sealed 가 아니다 — 다른 도메인 에러가 흘러들어올 가능성에
    // 대비한 최종 안전망. AuthError 외에 도달할 일은 없으므로 일반 문구만 노출한다.
    else -> UiError(
        title = UiText.Res(CommonR.string.error_title_generic),
        message = UiText.Res(CommonR.string.error_message_unknown),
    )
}

private fun AuthError.toUiError(): UiError = when (this) {
    is AuthError.InvalidCredentials -> UiError(
        title = UiText.Res(R.string.login_error_invalid_credentials_title),
        message = message,
    )
    is AuthError.RateLimited -> UiError(
        title = UiText.Res(CommonR.string.error_title_rate_limited),
        message = message,
    )
    is AuthError.SessionExpired -> UiError(
        // 로그인 화면 한정 — 토큰 만료를 일반 로그인 실패 톤으로 노출한다.
        title = UiText.Res(R.string.login_error_session_expired_title),
        message = message,
    )
    // 정상 흐름이면 클라가 사전 검증을 통과시키므로 도달 시 클라 버그 신호.
    // 사용자에게는 일반 입력 오류 톤으로만 노출 — 디버깅은 로그/traceId 로.
    is AuthError.ValidationFailed -> UiError(
        title = UiText.Res(R.string.login_error_validation_title),
        message = message,
    )
    is AuthError.NoConnection -> UiError(
        title = UiText.Res(CommonR.string.error_title_no_connection),
        message = message,
    )
    is AuthError.Timeout -> UiError(
        title = UiText.Res(CommonR.string.error_title_timeout),
        message = message,
    )
    is AuthError.ServerError -> UiError(
        title = UiText.Res(CommonR.string.error_title_server),
        message = message,
    )
    is AuthError.Unknown -> UiError(
        title = UiText.Res(CommonR.string.error_title_generic),
        message = message,
    )
}
