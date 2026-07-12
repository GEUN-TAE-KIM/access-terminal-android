package com.gtkim.mobile_access_control.core.database.master.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * master sync 로 받아온 user row (api-spec §8.1).
 *
 * PII 최소화 — `name` / `department` 는 wire 에는 오지만 mapper 단계에서 drop 한다
 * (hybrid-offline.md §2.1). 단말 분실 시 사원 명단 누출 표면 축소.
 *
 * PK 는 server PK (`Long`). `deletedIds[]` tombstone 적용을 위해 server PK 가 필요하며,
 * permission 의 `userId` join 도 이걸 그대로 사용한다. `employeeCode` 는 보조 인덱스.
 */
@Entity(
    tableName = "users",
    indices = [Index("employeeCode", unique = true)],
)
data class UserEntity(
    @PrimaryKey val id: Long,
    val employeeCode: String,
    val isActive: Boolean,
)
