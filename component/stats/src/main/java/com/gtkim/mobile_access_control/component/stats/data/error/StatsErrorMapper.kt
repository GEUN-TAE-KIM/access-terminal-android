package com.gtkim.mobile_access_control.component.stats.data.error

import com.gtkim.mobile_access_control.component.stats.domain.model.StatsError
import com.gtkim.mobile_access_control.core.network.error.NetworkError
import com.gtkim.mobile_access_control.core.network.error.toNetworkError

/**
 * Data 레이어 에러 매핑 — `Throwable` → [StatsError].
 *
 * 전송 계층 오류는 [toNetworkError] 로 분류하고, 서버 에러는 RFC 7807 `errorCode` 로 분기한다
 * (3-layer error mapping, architecture.md §4).
 */
internal fun Throwable.toStatsError(): StatsError = when (val net = toNetworkError()) {
    is NetworkError.NoConnection -> StatsError.NoConnection
    is NetworkError.Timeout -> StatsError.Timeout
    is NetworkError.Unknown -> StatsError.Unknown(net.cause)
    is NetworkError.Server -> when (net.detail?.errorCode) {
        "REQUEST_VALIDATION_FAILED" -> StatsError.ValidationFailed
        "AUTH_TOKEN_EXPIRED",
        "AUTH_TOKEN_INVALID",
        "AUTH_TOKEN_MISSING",
            -> StatsError.SessionExpired

        "AUTH_FORBIDDEN_ROLE" -> StatsError.Forbidden
        else -> StatsError.ServerError(net.detail?.errorCode)
    }
}
