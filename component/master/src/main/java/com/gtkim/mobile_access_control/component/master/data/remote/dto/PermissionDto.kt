package com.gtkim.mobile_access_control.component.master.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * snapshot 응답의 permission upserted 항목 (api-spec §8.1).
 *
 * raw 필드만 — 평가는 client `LocalAccessVerifier` 가 수행한다. `validUntil` 이 null 이면 무기한,
 * `allowedHours*` 가 모두 null 이면 24시간 허용. server 가 `is_active = TRUE` row 만 보내므로
 * client 는 비활성 필터링을 별도로 하지 않는다.
 *
 * `validFrom` 은 wire 명세에 없다 (§8.1 Field Semantics) — server 가 활성 권한만 보내는 게
 * "유효기간 시작" 의미를 사실상 흡수한다. server PermissionEvaluator 가 evaluate 단계에서
 * `validFrom` 을 검사하던 잔재는 별도 PR 로 제거 예정 (이 PR 묶음).
 */
@Serializable
internal data class PermissionDto(
    val id: Long,
    val userId: Long,
    val zone: String,
    val validUntil: String? = null,
    val allowedHoursStart: String? = null,
    val allowedHoursEnd: String? = null,
)
