package com.gtkim.mobile_access_control.core.database.master.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * master sync 로 받아온 zone catalog (api-spec §8.1, Phase 12 server-side).
 *
 * PK 는 server PK (`Long`). `deletedIds[]` tombstone 적용을 위해 server PK 가 필요.
 * `code` 는 unique — 단말이 verify 시 보낼 식별자라 catalog 내에서 충돌 없어야 한다.
 */
@Entity(
    tableName = "zones",
    indices = [Index("code", unique = true)],
)
data class ZoneEntity(
    @PrimaryKey val id: Long,
    val code: String,
    val name: String,
)
