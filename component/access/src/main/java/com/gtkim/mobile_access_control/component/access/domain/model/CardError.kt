package com.gtkim.mobile_access_control.component.access.domain.model

import com.gtkim.mobile_access_control.component.access.R
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.core.common.result.AppError

/**
 * 카드 등록(`POST /api/v1/access/cards`) data 레이어 도메인 에러.
 *
 * 서버 에러는 RFC 7807 `errorCode` 로 분기한다 (3-layer error mapping, architecture.md §4).
 */
sealed interface CardError : AppError {

    /** 404 `USER_NOT_FOUND` — `employeeCode` 가 시스템에 없음. */
    data object UserNotFound : CardError {
        override val message: UiText = UiText.Res(R.string.card_error_user_not_found)
    }

    /** 409 `CARD_ALREADY_REGISTERED` — 같은 `cardUid` 가 이미 등록됨. */
    data object CardAlreadyRegistered : CardError {
        override val message: UiText = UiText.Res(R.string.card_error_already_registered)
    }

    /** 422 `USER_INACTIVE` — 비활성 사용자에게 카드 등록 시도. */
    data object UserInactive : CardError {
        override val message: UiText = UiText.Res(R.string.card_error_user_inactive)
    }

    /** 403 `AUTH_FORBIDDEN_ROLE` — OPERATOR 권한으로 시도 (ADMIN 만 가능). */
    data object ForbiddenRole : CardError {
        override val message: UiText = UiText.Res(R.string.card_error_forbidden_role)
    }

    /** 400 `REQUEST_VALIDATION_FAILED` / `REQUEST_MALFORMED_JSON` — 필드 오류, MOCK cardType 시도 등. */
    data object ValidationFailed : CardError {
        override val message: UiText = UiText.Res(R.string.card_error_validation_failed)
    }

    /** 401 — Access Token 만료·무효, 자동 갱신 실패. 재로그인 필요. */
    data object SessionExpired : CardError {
        override val message: UiText = UiText.Res(R.string.card_error_session_expired)
    }

    /** 429 `RATE_LIMIT_EXCEEDED` — 카드 등록 Rate Limit (사용자 당 30회/분, 명세 §4.2). */
    data object RateLimited : CardError {
        override val message: UiText = UiText.Res(R.string.card_error_rate_limited)
    }

    data object NoConnection : CardError {
        override val message: UiText = UiText.Res(R.string.card_error_no_connection)
    }

    data object Timeout : CardError {
        override val message: UiText = UiText.Res(R.string.card_error_timeout)
    }

    /** 그 외 서버 에러 — 미분류 `errorCode` 보존. */
    data class ServerError(val errorCode: String?) : CardError {
        override val message: UiText = UiText.Res(R.string.card_error_server)
    }

    /** 분류 실패 — [cause] 는 진단용으로 보존, 사용자 노출은 일반 문구로 통일. */
    data class Unknown(val cause: Throwable) : CardError {
        override val message: UiText = UiText.Res(R.string.card_error_unknown)
    }
}
