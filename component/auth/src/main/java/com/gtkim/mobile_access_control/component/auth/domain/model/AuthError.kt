package com.gtkim.mobile_access_control.component.auth.domain.model

import com.gtkim.mobile_access_control.component.auth.R
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.core.common.result.AppError

sealed interface AuthError : AppError {
    /** `AUTH_INVALID_CREDENTIALS` — ID/비밀번호 불일치. */
    data object InvalidCredentials : AuthError {
        override val message: UiText = UiText.Res(R.string.auth_error_invalid_credentials)
    }

    /** `RATE_LIMIT_EXCEEDED` — 로그인 시도 과다. */
    data object RateLimited : AuthError {
        override val message: UiText = UiText.Res(R.string.auth_error_rate_limited)
    }

    /** 400 `REQUEST_VALIDATION_FAILED` — 필드 형식 오류. 정상 흐름에서는 클라가 사전 검증을 통과시키므로 클라 버그 신호다. */
    data object ValidationFailed : AuthError {
        override val message: UiText = UiText.Res(R.string.auth_error_validation_failed)
    }

    /** Refresh Token 만료·폐기·무효 — 재로그인 필요. */
    data object SessionExpired : AuthError {
        override val message: UiText = UiText.Res(R.string.auth_error_session_expired)
    }

    data object NoConnection : AuthError {
        override val message: UiText = UiText.Res(R.string.auth_error_no_connection)
    }

    data object Timeout : AuthError {
        override val message: UiText = UiText.Res(R.string.auth_error_timeout)
    }

    /** 그 외 서버 오류 (5xx, 클라 측 검증 실패 등). */
    data class ServerError(val errorCode: String?) : AuthError {
        override val message: UiText = UiText.Res(R.string.auth_error_server)
    }

    /** 분류 실패 — [cause] 는 진단용으로 보존, 사용자 노출은 일반 문구로 통일. */
    data class Unknown(val cause: Throwable) : AuthError {
        override val message: UiText = UiText.Res(R.string.auth_error_unknown)
    }
}
