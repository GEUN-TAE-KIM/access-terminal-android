package com.gtkim.mobile_access_control.component.auth.data.error

import com.gtkim.mobile_access_control.component.auth.domain.model.AuthError
import com.gtkim.mobile_access_control.core.network.error.NetworkError
import com.gtkim.mobile_access_control.core.network.error.toNetworkError

/**
 * Data 레이어 에러 매핑 — `Throwable` → [AuthError].
 *
 * 전송 계층 오류는 [toNetworkError] 로 분류하고, 서버 에러는 RFC 7807 `errorCode` 로 분기한다.
 */
internal fun Throwable.toAuthError(): AuthError = when (val net = toNetworkError()) {
    is NetworkError.NoConnection -> AuthError.NoConnection
    is NetworkError.Timeout -> AuthError.Timeout
    is NetworkError.Unknown -> AuthError.Unknown(net.cause)
    is NetworkError.Server -> when (net.detail?.errorCode) {
        "AUTH_INVALID_CREDENTIALS" -> AuthError.InvalidCredentials
        "RATE_LIMIT_EXCEEDED" -> AuthError.RateLimited
        "REQUEST_VALIDATION_FAILED",
        "REQUEST_MALFORMED_JSON",
            -> AuthError.ValidationFailed

        "AUTH_TOKEN_MISSING",
        "AUTH_TOKEN_INVALID",
        "AUTH_REFRESH_EXPIRED",
        "AUTH_REFRESH_REVOKED",
            -> AuthError.SessionExpired

        else -> AuthError.ServerError(net.detail?.errorCode)
    }
}
