package com.gtkim.mobile_access_control.component.history.data.error

import com.gtkim.mobile_access_control.component.history.domain.model.HistoryError
import com.gtkim.mobile_access_control.core.network.error.NetworkError
import com.gtkim.mobile_access_control.core.network.error.toNetworkError

/**
 * Data 레이어 에러 매핑 — `Throwable` → [HistoryError].
 *
 * 전송 계층 오류는 [toNetworkError] 로 분류하고, 서버 에러는 RFC 7807 `errorCode` 로 분기한다
 * (3-layer error mapping, architecture.md §4).
 */
internal fun Throwable.toHistoryError(): HistoryError = when (val net = toNetworkError()) {
    is NetworkError.NoConnection -> HistoryError.NoConnection
    is NetworkError.Timeout -> HistoryError.Timeout
    is NetworkError.Unknown -> HistoryError.Unknown(net.cause)
    is NetworkError.Server -> when (net.detail?.errorCode) {
        "REQUEST_VALIDATION_FAILED",
        "REQUEST_MALFORMED_JSON",
            -> HistoryError.ValidationFailed

        "AUTH_TOKEN_EXPIRED",
        "AUTH_TOKEN_INVALID",
        "AUTH_TOKEN_MISSING",
            -> HistoryError.SessionExpired

        "AUTH_FORBIDDEN_ROLE" -> HistoryError.Forbidden
        else -> HistoryError.ServerError(net.detail?.errorCode)
    }
}
