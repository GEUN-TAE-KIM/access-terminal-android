package com.gtkim.mobile_access_control.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gtkim.mobile_access_control.core.database.master.dao.CardDao
import com.gtkim.mobile_access_control.core.database.master.dao.MasterDao
import com.gtkim.mobile_access_control.core.database.master.dao.PermissionDao
import com.gtkim.mobile_access_control.core.database.master.dao.UserDao
import com.gtkim.mobile_access_control.core.database.master.dao.ZoneDao
import com.gtkim.mobile_access_control.core.database.master.entity.CardEntity
import com.gtkim.mobile_access_control.core.database.master.entity.MasterMetaEntity
import com.gtkim.mobile_access_control.core.database.master.entity.PermissionEntity
import com.gtkim.mobile_access_control.core.database.master.entity.UserEntity
import com.gtkim.mobile_access_control.core.database.master.entity.ZoneEntity
import com.gtkim.mobile_access_control.core.database.sync.dao.PendingLogDao
import com.gtkim.mobile_access_control.core.database.sync.entity.PendingLogEntity

/**
 * 모든 비즈니스 패키지의 DAO 를 통합 관리하는 단일 RoomDatabase.
 * :component 모듈 내부 전용 (internal) — :feature / :app 에서 직접 접근 금지.
 *
 * 새 persistent 비즈니스 패키지 추가 절차:
 * 1) 해당 패키지의 data/local 에 entity + dao 작성 (internal)
 * 2) 아래 entities 배열에 *Entity 추가 + abstract fun <x>Dao() 선언
 * 3) DatabaseModule 에 @Provides fun <x>Dao(db) 추가
 * 4) version 증가 + Migration 작성 (또는 fallbackToDestructiveMigration)
 */
@Database(
    entities = [
        UserEntity::class,
        CardEntity::class,
        PermissionEntity::class,
        ZoneEntity::class,
        MasterMetaEntity::class,
        PendingLogEntity::class,
    ],
    version = 1,
    // destructive migration + single release 라 마이그레이션·스키마 이력이 없다 → export 한 JSON 을
    // 소비할 곳이 없으므로 false. (true 면 schemaLocation 미설정 경고만 발생.)
    exportSchema = false,
)
internal abstract class AccessDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun cardDao(): CardDao
    abstract fun permissionDao(): PermissionDao
    abstract fun zoneDao(): ZoneDao
    abstract fun masterDao(): MasterDao
    abstract fun pendingLogDao(): PendingLogDao

    companion object {
        const val NAME = "access.db"
    }
}
