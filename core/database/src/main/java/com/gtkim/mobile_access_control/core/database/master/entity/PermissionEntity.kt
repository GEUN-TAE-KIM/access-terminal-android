package com.gtkim.mobile_access_control.core.database.master.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * master sync 로 받아온 permission row (api-spec §8.1).
 *
 * `allowedHoursStart` / `allowedHoursEnd` 는 JST 기준 `HH:mm:ss` 문자열로 저장 — Room 의 LocalTime
 * 직접 지원이 없고 JST 라는 단일 ZoneId 가 명세에 박혀 있어 string 보관이 단순하다. 평가 단계에서
 * `LocalTime.parse` 로 복원.
 *
 * `validFrom` 은 wire 명세에 없다 (§8.1) — server 가 `is_active = TRUE` row 만 보내는 것이 사실상
 * "이미 시작된 권한" 을 의미한다. `validUntil` 만 client 가 평가한다.
 */
@Entity(
    tableName = "permissions",
    indices = [
        Index("userId"),
        Index(value = ["userId", "zone"]),
    ],
)
data class PermissionEntity(
    @PrimaryKey val id: Long,
    val userId: Long,
    val zone: String,
    val validUntilEpochMs: Long?,
    val allowedHoursStart: String?,
    val allowedHoursEnd: String?,
)
