package com.gtkim.mobile_access_control.component.history.domain.model

import com.gtkim.mobile_access_control.component.history.R
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.core.common.result.AppError

/**
 * 출입 기록 조회(`GET /api/v1/access/logs`) data 레이어 도메인 에러.
 *
 * 서버 에러는 RFC 7807 `errorCode` 로 분기한다 (3-layer error mapping, architecture.md §4).
 */
sealed interface HistoryError : AppError {

    /** 400 `REQUEST_VALIDATION_FAILED` / `REQUEST_MALFORMED_JSON` — 잘못된 파라미터·cursor. */
    data object ValidationFailed : HistoryError {
        override val message: UiText = UiText.Res(R.string.history_error_validation_failed)
    }

    /** 401 — Access Token 만료·무효, 자동 갱신 실패. 재로그인 필요. */
    data object SessionExpired : HistoryError {
        override val message: UiText = UiText.Res(R.string.history_error_session_expired)
    }

    /** 403 `AUTH_FORBIDDEN_ROLE` — 관리자 권한 없음. */
    data object Forbidden : HistoryError {
        override val message: UiText = UiText.Res(R.string.history_error_forbidden)
    }

    data object NoConnection : HistoryError {
        override val message: UiText = UiText.Res(R.string.history_error_no_connection)
    }

    data object Timeout : HistoryError {
        override val message: UiText = UiText.Res(R.string.history_error_timeout)
    }

    /** 그 외 서버 에러 — 미분류 `errorCode` 보존. */
    data class ServerError(val errorCode: String?) : HistoryError {
        override val message: UiText = UiText.Res(R.string.history_error_server)
    }

    /** 분류 실패 — [cause] 는 진단용으로 보존, 사용자 노출은 일반 문구로 통일. */
    data class Unknown(val cause: Throwable) : HistoryError {
        override val message: UiText = UiText.Res(R.string.history_error_unknown)
    }
}
