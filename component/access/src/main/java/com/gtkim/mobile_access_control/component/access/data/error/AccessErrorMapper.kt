package com.gtkim.mobile_access_control.component.access.data.error

import com.gtkim.mobile_access_control.component.access.domain.model.AccessError
import com.gtkim.mobile_access_control.core.network.error.NetworkError
import com.gtkim.mobile_access_control.core.network.error.toNetworkError

/**
 * Data 레이어 에러 매핑 — `Throwable` → [AccessError].
 *
 * 전송 계층 오류는 [toNetworkError] 로 분류하고, 서버 에러는 RFC 7807 `errorCode` 로 분기한다
 * (3-layer error mapping, architecture.md §4).
 */
internal fun Throwable.toAccessError(): AccessError = when (val net = toNetworkError()) {
    is NetworkError.NoConnection -> AccessError.NoConnection
    is NetworkError.Timeout -> AccessError.Timeout
    is NetworkError.Unknown -> AccessError.Unknown(net.cause)
    is NetworkError.Server ->
        when (net.detail?.errorCode) {
            "ACCESS_CARD_NOT_REGISTERED" -> AccessError.CardNotRegistered
            "ACCESS_REPLAY_DETECTED" -> AccessError.ReplayDetected
            "ACCESS_TIMESTAMP_SKEW" -> AccessError.TimestampSkew
            "ACCESS_IDEMPOTENCY_CONFLICT" -> AccessError.IdempotencyConflict
            "ACCESS_INVALID_CARD_TYPE" -> AccessError.InvalidCardType
            "REQUEST_VALIDATION_FAILED",
            "REQUEST_MALFORMED_JSON",
                -> AccessError.ValidationFailed

            "REQUEST_MISSING_HEADER" -> AccessError.MissingHeader
            "AUTH_TOKEN_EXPIRED",
            "AUTH_TOKEN_INVALID",
            "AUTH_TOKEN_MISSING",
                -> AccessError.SessionExpired

            "RATE_LIMIT_EXCEEDED" -> AccessError.RateLimited
            else -> AccessError.ServerError(net.detail?.errorCode)
        }
}
