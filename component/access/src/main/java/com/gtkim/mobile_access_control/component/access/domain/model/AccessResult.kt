package com.gtkim.mobile_access_control.component.access.domain.model

import com.gtkim.mobile_access_control.core.model.DenyReason
import java.time.Instant

/**
 * verify 정상 응답(`200 OK`). 허가/거부 모두 동일 형태로 반환된다 — 거부도 에러가 아니다 (API 명세 §4.1).
 *
 * 검증 자체가 수행되지 못한 경우(미등록 카드, 시계 오차 등)는 [AccessError] 로 분리된다.
 */
data class AccessResult(
    val decision: AccessDecision,
    /** 출입 기록 PK (`access_logs.id`). */
    val logId: Long,
    val user: AccessUser,
    /** 거부 사유. [decision] 이 [AccessDecision.ALLOWED] 면 `null`. */
    val denyReason: DenyReason?,
    /**
     * 권한(permission)의 만료 절대 시각. 허가 응답에만 존재하며,
     * `null` 이면 무기한 권한이다 (카드 만료나 일일 허용시간이 아님, API 명세 §4.1).
     */
    val validUntil: Instant?,
    /** 검증 대상 시도가 발생한 시각 (= 요청 본문 `timestamp`). */
    val verifiedAt: Instant,
)
