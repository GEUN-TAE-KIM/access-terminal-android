package com.gtkim.mobile_access_control.component.access.domain.model

import com.gtkim.mobile_access_control.component.access.R
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.core.common.result.AppError

/**
 * verify data 레이어 도메인 에러.
 *
 * 출입 거부(`DENIED_*`)는 에러가 아니라 [AccessResult] 다. 여기 모이는 건 검증 자체가
 * 수행되지 못한 경우뿐이다 (API 명세 §4.1). 서버 에러는 RFC 7807 `errorCode` 로 분기한다.
 */
sealed interface AccessError : AppError {

    /** 404 `ACCESS_CARD_NOT_REGISTERED` — 시스템에 등록되지 않은 카드. */
    data object CardNotRegistered : AccessError {
        override val message: UiText = UiText.Res(R.string.access_error_card_not_registered)
    }

    /** 409 `ACCESS_REPLAY_DETECTED` — nonce 중복. 새 nonce 로 재시도. */
    data object ReplayDetected : AccessError {
        override val message: UiText = UiText.Res(R.string.access_error_replay_detected)
    }

    /** 400 `ACCESS_TIMESTAMP_SKEW` — 단말 시계가 서버와 5분 초과 차이. */
    data object TimestampSkew : AccessError {
        override val message: UiText = UiText.Res(R.string.access_error_timestamp_skew)
    }

    /** 409 `ACCESS_IDEMPOTENCY_CONFLICT` — 같은 키에 다른 본문 (클라이언트 버그). */
    data object IdempotencyConflict : AccessError {
        override val message: UiText = UiText.Res(R.string.access_error_idempotency_conflict)
    }

    /** 400 `ACCESS_INVALID_CARD_TYPE` — 알 수 없는 cardType. */
    data object InvalidCardType : AccessError {
        override val message: UiText = UiText.Res(R.string.access_error_invalid_card_type)
    }

    /** 400 `REQUEST_VALIDATION_FAILED` / `REQUEST_MALFORMED_JSON` — 필드 형식·JSON 오류. 클라 버그 신호. */
    data object ValidationFailed : AccessError {
        override val message: UiText = UiText.Res(R.string.access_error_validation_failed)
    }

    /** 400 `REQUEST_MISSING_HEADER` — `Idempotency-Key` 등 필수 헤더 누락. 클라 버그 신호. */
    data object MissingHeader : AccessError {
        override val message: UiText = UiText.Res(R.string.access_error_missing_header)
    }

    /** 401 — Access Token 만료·무효, 자동 갱신 실패. 재로그인 필요. */
    data object SessionExpired : AccessError {
        override val message: UiText = UiText.Res(R.string.access_error_session_expired)
    }

    /** 429 `RATE_LIMIT_EXCEEDED` — 단말 Rate Limit 초과. */
    data object RateLimited : AccessError {
        override val message: UiText = UiText.Res(R.string.access_error_rate_limited)
    }

    data object NoConnection : AccessError {
        override val message: UiText = UiText.Res(R.string.access_error_no_connection)
    }

    data object Timeout : AccessError {
        override val message: UiText = UiText.Res(R.string.access_error_timeout)
    }

    /** 그 외 서버 에러 — 미분류 `errorCode` 보존. */
    data class ServerError(val errorCode: String?) : AccessError {
        override val message: UiText = UiText.Res(R.string.access_error_server)
    }

    /** 분류 실패 — [cause] 는 진단용으로 보존, 사용자 노출은 일반 문구로 통일. */
    data class Unknown(val cause: Throwable) : AccessError {
        override val message: UiText = UiText.Res(R.string.access_error_unknown)
    }
}
