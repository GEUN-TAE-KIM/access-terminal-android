package com.gtkim.mobile_access_control.component.auth.domain.model

/**
 * 관리자 권한 등급.
 *
 * - [ADMIN] — 카드 발급·해지 등 관리 작업 가능
 * - [OPERATOR] — 현장 검문만 가능
 */
enum class AdminRole {
    ADMIN,
    OPERATOR,
    ;

    companion object {
        /** 알 수 없는 값은 최소 권한([OPERATOR])으로 안전하게 폴백. */
        fun fromWire(raw: String): AdminRole =
            entries.firstOrNull { it.name == raw } ?: OPERATOR
    }
}
