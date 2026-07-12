package com.gtkim.mobile_access_control.component.history.domain.model

/**
 * 출입 기록의 판정 결과 (API 명세 §5.1 응답 `items[].result`).
 *
 * verify 응답의 `result` 와 같은 어휘다. `:component:history` 는 `:component:access` 에
 * 의존하지 않으므로 (모듈 경계) 별도 정의한다.
 */
enum class LogResult {
    ALLOWED,
    DENIED_NO_PERMISSION,
    DENIED_OUT_OF_HOURS,
    DENIED_EXPIRED,
    DENIED_INACTIVE_CARD,
    DENIED_INACTIVE_USER,

    /** 클라이언트가 모르는 신규 result — forward-compat. */
    UNKNOWN;

    companion object {
        fun fromWire(raw: String): LogResult =
            entries.firstOrNull { it.name == raw } ?: UNKNOWN
    }
}
