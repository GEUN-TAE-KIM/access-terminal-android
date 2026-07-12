package com.gtkim.mobile_access_control.component.master.domain.model

import com.gtkim.mobile_access_control.core.model.Zone
import java.time.Instant
import java.time.LocalTime

/**
 * 캐시된 permission. raw 필드 — 평가는 client `LocalAccessVerifier` 가 수행한다.
 *
 * [validUntil] = null 이면 무기한. [allowedHoursStart]·[allowedHoursEnd] 가 모두 null 이면
 * 24시간 허용 (api-spec §8.1).
 */
data class CachedPermission(
    val id: Long,
    val userId: Long,
    val zone: Zone,
    val validUntil: Instant?,
    val allowedHoursStart: LocalTime?,
    val allowedHoursEnd: LocalTime?,
) {
    /** 24시간 허용 권한인지 (server `PermissionEntity.isAllDay` 와 동치). */
    val isAllDay: Boolean
        get() = allowedHoursStart == null && allowedHoursEnd == null
}
