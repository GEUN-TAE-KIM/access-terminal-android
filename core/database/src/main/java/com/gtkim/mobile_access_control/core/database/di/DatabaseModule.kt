package com.gtkim.mobile_access_control.core.database.di

import android.content.Context
import androidx.room.Room
import com.gtkim.mobile_access_control.core.database.master.dao.CardDao
import com.gtkim.mobile_access_control.core.database.master.dao.MasterDao
import com.gtkim.mobile_access_control.core.database.master.dao.PermissionDao
import com.gtkim.mobile_access_control.core.database.master.dao.UserDao
import com.gtkim.mobile_access_control.core.database.master.dao.ZoneDao
import com.gtkim.mobile_access_control.core.database.sync.dao.PendingLogDao
import com.gtkim.mobile_access_control.core.database.AccessDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    internal fun database(@ApplicationContext context: Context): AccessDatabase =
        Room.databaseBuilder(context, AccessDatabase::class.java, AccessDatabase.NAME)
            // 본 프로젝트 스코프는 single release — 마이그레이션 없이 destructive 로 충분 (architecture.md §8).
            // dropAllTables = true: schema 변경 시 모든 테이블 drop (FK 무관). master 캐시는 다음
            // sync 로 즉시 재구성되고, pending_logs 는 복구 불가하지만 single release 스코프상 허용.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides internal fun userDao(db: AccessDatabase): UserDao = db.userDao()
    @Provides internal fun cardDao(db: AccessDatabase): CardDao = db.cardDao()
    @Provides internal fun permissionDao(db: AccessDatabase): PermissionDao = db.permissionDao()
    @Provides internal fun zoneDao(db: AccessDatabase): ZoneDao = db.zoneDao()
    @Provides internal fun masterDao(db: AccessDatabase): MasterDao = db.masterDao()
    @Provides internal fun pendingLogDao(db: AccessDatabase): PendingLogDao = db.pendingLogDao()
}
