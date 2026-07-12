package com.gtkim.mobile_access_control.core.model

/**
 * 출입 거부 사유 enum (API 명세 §4.1 `denyReason`). 검문 / 히스토리 / 통계 / offline LocalAccessVerifier
 * 가 모두 같은 셋을 공유한다 (서버 access_logs 의 단일 enum). 모듈별 중복 정의를 막기 위해 `:core/model/`
 * 에 통합 — value class 들 (`CardUid`, `Zone` 등) 과 같이 도메인 모델 위치.
 *
 * wire 포맷 (`ACCESS_DENIED_*` / `ACCESS_CARD_NOT_REGISTERED`) ↔ enum 변환은 본 객체의
 * [fromWire] / [toWire] 가 단일 책임. 사용자 노출 한국어 라벨은 각 feature 모듈의
 * `DenyReasonLabel.kt` 가 presentation 으로 매핑.
 */
enum class DenyReason {
    NO_PERMISSION_FOR_ZONE,
    OUT_OF_ALLOWED_HOURS,
    PERMISSION_EXPIRED,
    CARD_REVOKED,
    USER_INACTIVE,

    /**
     * 미등록 카드 시도. 명세상 verify API 의 `errorCode (404 ACCESS_CARD_NOT_REGISTERED)` 이지만
     * 서버가 access_logs 에 같은 코드로 기록해 history / stats 응답에도 떨어진다 (운영 관점에서
     * "오늘 미등록 카드 시도 N건" 이 유의미). 서버 LogQueryParser 가 [entries] 전체를 받으므로
     * 필터로도 통과.
     */
    CARD_NOT_REGISTERED,

    /** 클라이언트가 모르는 신규 코드 — forward-compat. */
    UNKNOWN;

    /**
     * 서버 쿼리 파라미터 / 요청 본문 직렬화.
     * UNKNOWN 은 클라가 선택할 수 없는 값이라 `null` (필터에서 제외됨).
     */
    fun toWire(): String? = when (this) {
        NO_PERMISSION_FOR_ZONE -> "ACCESS_DENIED_NO_PERMISSION_FOR_ZONE"
        OUT_OF_ALLOWED_HOURS -> "ACCESS_DENIED_OUT_OF_ALLOWED_HOURS"
        PERMISSION_EXPIRED -> "ACCESS_DENIED_PERMISSION_EXPIRED"
        CARD_REVOKED -> "ACCESS_DENIED_CARD_REVOKED"
        USER_INACTIVE -> "ACCESS_DENIED_USER_INACTIVE"
        CARD_NOT_REGISTERED -> "ACCESS_CARD_NOT_REGISTERED"
        UNKNOWN -> null
    }

    companion object {
        /** UI 드롭다운에서 노출할 사용자 선택 가능 사유 — UNKNOWN 만 제외. */
        val selectable: List<DenyReason> = listOf(
            NO_PERMISSION_FOR_ZONE,
            OUT_OF_ALLOWED_HOURS,
            PERMISSION_EXPIRED,
            CARD_REVOKED,
            USER_INACTIVE,
            CARD_NOT_REGISTERED,
        )

        fun fromWire(raw: String): DenyReason = when (raw) {
            "ACCESS_DENIED_NO_PERMISSION_FOR_ZONE" -> NO_PERMISSION_FOR_ZONE
            "ACCESS_DENIED_OUT_OF_ALLOWED_HOURS" -> OUT_OF_ALLOWED_HOURS
            "ACCESS_DENIED_PERMISSION_EXPIRED" -> PERMISSION_EXPIRED
            "ACCESS_DENIED_CARD_REVOKED" -> CARD_REVOKED
            "ACCESS_DENIED_USER_INACTIVE" -> USER_INACTIVE
            "ACCESS_CARD_NOT_REGISTERED" -> CARD_NOT_REGISTERED
            else -> UNKNOWN
        }
    }
}
