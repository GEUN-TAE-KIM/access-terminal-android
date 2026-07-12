package com.gtkim.mobile_access_control.core.database.master.dao

import androidx.room.Dao
import androidx.room.Query
import com.gtkim.mobile_access_control.core.database.master.entity.PermissionEntity

@Dao
interface PermissionDao {
    @Query("SELECT * FROM permissions WHERE userId = :userId AND zone = :zone")
    suspend fun byUserAndZone(userId: Long, zone: String): List<PermissionEntity>
}
