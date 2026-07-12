package com.gtkim.mobile_access_control.component.access.data.error

import com.gtkim.mobile_access_control.component.access.domain.model.CardError
import com.gtkim.mobile_access_control.core.network.error.NetworkError
import com.gtkim.mobile_access_control.core.network.error.toNetworkError

/**
 * Data 레이어 에러 매핑 — `Throwable` → [CardError].
 *
 * 전송 계층 오류는 [toNetworkError] 로 분류하고, 서버 에러는 RFC 7807 `errorCode` 로 분기한다
 * (3-layer error mapping, architecture.md §4).
 */
internal fun Throwable.toCardError(): CardError = when (val net = toNetworkError()) {
    is NetworkError.NoConnection -> CardError.NoConnection
    is NetworkError.Timeout -> CardError.Timeout
    is NetworkError.Unknown -> CardError.Unknown(net.cause)
    is NetworkError.Server -> when (net.detail?.errorCode) {
        "USER_NOT_FOUND" -> CardError.UserNotFound
        "CARD_ALREADY_REGISTERED" -> CardError.CardAlreadyRegistered
        "USER_INACTIVE" -> CardError.UserInactive
        "AUTH_FORBIDDEN_ROLE" -> CardError.ForbiddenRole
        "REQUEST_VALIDATION_FAILED",
        "REQUEST_MALFORMED_JSON",
            -> CardError.ValidationFailed

        "AUTH_TOKEN_EXPIRED",
        "AUTH_TOKEN_INVALID",
        "AUTH_TOKEN_MISSING",
            -> CardError.SessionExpired

        "RATE_LIMIT_EXCEEDED" -> CardError.RateLimited
        else -> CardError.ServerError(net.detail?.errorCode)
    }
}
