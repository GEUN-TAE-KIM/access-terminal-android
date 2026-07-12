package com.gtkim.mobile_access_control.core.database.master.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gtkim.mobile_access_control.core.database.master.entity.CardEntity
import com.gtkim.mobile_access_control.core.database.master.entity.MasterMetaEntity
import com.gtkim.mobile_access_control.core.database.master.entity.PermissionEntity
import com.gtkim.mobile_access_control.core.database.master.entity.UserEntity
import com.gtkim.mobile_access_control.core.database.master.entity.ZoneEntity

/**
 * snapshot delta (upserted + deletedIds) 를 master 테이블에 원자적으로 적용한다.
 *
 * Room 의 `@Transaction` 은 같은 DAO 안의 메서드들만 묶을 수 있어, batch 용 쿼리를 본 DAO 에
 * 자체적으로 보유한다. 일반 lookup 은 [UserDao] / [CardDao] / [PermissionDao] 에서 책임진다.
 *
 * snapshot ETag([MasterMetaEntity]) 도 캐시 row 와 **같은 트랜잭션**으로 기록한다 — 캐시만
 * 갱신되고 ETag 가 따로 노는 desync(캐시 wipe 후 ETag 잔존 → 다음 sync 304 로 빈 캐시 신뢰) 차단.
 */
@Dao
abstract class MasterDao {

    @Transaction
    open suspend fun applySnapshot(
        eTag: String,
        usersUpserted: List<UserEntity>,
        usersDeletedIds: List<Long>,
        cardsUpserted: List<CardEntity>,
        cardsDeletedIds: List<Long>,
        permissionsUpserted: List<PermissionEntity>,
        permissionsDeletedIds: List<Long>,
        zonesUpserted: List<ZoneEntity>,
        zonesDeletedIds: List<Long>,
    ) {
        if (permissionsDeletedIds.isNotEmpty()) deletePermissions(permissionsDeletedIds)
        if (cardsDeletedIds.isNotEmpty()) deleteCards(cardsDeletedIds)
        if (usersDeletedIds.isNotEmpty()) deleteUsers(usersDeletedIds)
        if (zonesDeletedIds.isNotEmpty()) deleteZones(zonesDeletedIds)

        if (usersUpserted.isNotEmpty()) upsertUsers(usersUpserted)
        if (cardsUpserted.isNotEmpty()) upsertCards(cardsUpserted)
        if (permissionsUpserted.isNotEmpty()) upsertPermissions(permissionsUpserted)
        if (zonesUpserted.isNotEmpty()) upsertZones(zonesUpserted)

        upsertMeta(MasterMetaEntity(eTag = eTag))
    }

    /** 단말이 마지막으로 적용한 snapshot ETag. 한 번도 sync 안 했으면 null (→ full sync). */
    @Query("SELECT eTag FROM master_meta WHERE id = 0 LIMIT 1")
    abstract suspend fun currentETag(): String?

    /** 304 경로 — 캐시는 그대로 두고 ETag 만 갱신. */
    open suspend fun updateETag(eTag: String) = upsertMeta(MasterMetaEntity(eTag = eTag))

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsertUsers(rows: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsertCards(rows: List<CardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsertPermissions(rows: List<PermissionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsertZones(rows: List<ZoneEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsertMeta(meta: MasterMetaEntity)

    @Query("DELETE FROM users WHERE id IN (:ids)")
    protected abstract suspend fun deleteUsers(ids: List<Long>)

    @Query("DELETE FROM cards WHERE id IN (:ids)")
    protected abstract suspend fun deleteCards(ids: List<Long>)

    @Query("DELETE FROM permissions WHERE id IN (:ids)")
    protected abstract suspend fun deletePermissions(ids: List<Long>)

    @Query("DELETE FROM zones WHERE id IN (:ids)")
    protected abstract suspend fun deleteZones(ids: List<Long>)
}
