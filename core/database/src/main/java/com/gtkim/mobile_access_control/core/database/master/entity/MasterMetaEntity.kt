package com.gtkim.mobile_access_control.core.database.master.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * master sync 메타 (단일 행). 현재는 마지막으로 적용한 snapshot ETag 하나만 보관한다.
 *
 * ETag 를 master 캐시(users/cards/permissions/zones)와 **같은 Room DB** 에 두는 이유:
 * destructive migration / 캐시 손상 복구 시 캐시 테이블과 ETag 가 항상 함께 비워져야 한다.
 * ETag 를 따로(SharedPreferences 등) 두면 캐시만 wipe 되고 ETag 가 살아남아, 다음 sync 가
 * If-None-Match 로 304 를 받고 비어버린 캐시를 그대로 신뢰하는 desync 가 발생한다.
 *
 * 단일 행 보장: [SINGLETON_ID] 고정 PK. "아직 sync 한 적 없음" 은 행 부재(=null) 로 표현한다.
 */
@Entity(tableName = "master_meta")
data class MasterMetaEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val eTag: String,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
