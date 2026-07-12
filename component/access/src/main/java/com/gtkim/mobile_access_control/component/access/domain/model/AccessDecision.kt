package com.gtkim.mobile_access_control.component.access.domain.model

/**
 * verify 응답의 `result` — 출입 판정.
 *
 * 출입 거부(`DENIED_*`)는 에러가 아니라 정상 응답이다 (`200 OK`, API 명세 §4.1).
 * 클라이언트는 try/catch 가 아니라 이 enum 으로 분기한다.
 */
enum class AccessDecision {
    ALLOWED,
    DENIED_NO_PERMISSION,
    DENIED_OUT_OF_HOURS,
    DENIED_EXPIRED,
    DENIED_INACTIVE_CARD,
    DENIED_INACTIVE_USER,

    /** 클라이언트가 모르는 신규 `result` — forward-compat. */
    UNKNOWN;

    val isAllowed: Boolean get() = this == ALLOWED

    companion object {
        fun fromWire(raw: String): AccessDecision =
            entries.firstOrNull { it.name == raw } ?: UNKNOWN
    }
}
