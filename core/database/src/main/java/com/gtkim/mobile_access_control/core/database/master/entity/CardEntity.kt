package com.gtkim.mobile_access_control.core.database.master.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * master sync 로 받아온 card row (api-spec §8.1).
 *
 * PK 는 server PK (`Long`) — tombstone 적용 일관성. `cardUid` 는 UNIQUE 보조 인덱스 (검문 시
 * UID 로 lookup).
 *
 * [cardType] 은 wire enum 문자열 (`FELICA` / `ISO_DEP` / `NDEF`). MOCK 은 server 가 보내지 않으므로
 * 캐시에도 들어오지 않는다.
 */
@Entity(
    tableName = "cards",
    indices = [
        Index("cardUid", unique = true),
        Index("userId"),
    ],
)
data class CardEntity(
    @PrimaryKey val id: Long,
    val cardUid: String,
    val cardType: String,
    val userId: Long,
    val isActive: Boolean,
)
