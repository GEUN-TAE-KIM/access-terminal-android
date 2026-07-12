package com.gtkim.mobile_access_control.component.stats.domain.model

import com.gtkim.mobile_access_control.component.stats.R
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.core.common.result.AppError

/**
 * 통계 조회(`GET /api/v1/access/stats`) data 레이어 도메인 에러.
 *
 * 서버 에러는 RFC 7807 `errorCode` 로 분기한다 (3-layer error mapping, architecture.md §4).
 */
sealed interface StatsError : AppError {

    /** 400 `REQUEST_VALIDATION_FAILED` — date 누락·형식 오류. */
    data object ValidationFailed : StatsError {
        override val message: UiText = UiText.Res(R.string.stats_error_validation_failed)
    }

    /** 401 — Access Token 만료·무효, 자동 갱신 실패. 재로그인 필요. */
    data object SessionExpired : StatsError {
        override val message: UiText = UiText.Res(R.string.stats_error_session_expired)
    }

    /** 403 `AUTH_FORBIDDEN_ROLE` — 관리자 권한 없음. */
    data object Forbidden : StatsError {
        override val message: UiText = UiText.Res(R.string.stats_error_forbidden)
    }

    data object NoConnection : StatsError {
        override val message: UiText = UiText.Res(R.string.stats_error_no_connection)
    }

    data object Timeout : StatsError {
        override val message: UiText = UiText.Res(R.string.stats_error_timeout)
    }

    /** 그 외 서버 에러 — 미분류 `errorCode` 보존. */
    data class ServerError(val errorCode: String?) : StatsError {
        override val message: UiText = UiText.Res(R.string.stats_error_server)
    }

    /** 분류 실패 — [cause] 는 진단용으로 보존, 사용자 노출은 일반 문구로 통일. */
    data class Unknown(val cause: Throwable) : StatsError {
        override val message: UiText = UiText.Res(R.string.stats_error_unknown)
    }
}
