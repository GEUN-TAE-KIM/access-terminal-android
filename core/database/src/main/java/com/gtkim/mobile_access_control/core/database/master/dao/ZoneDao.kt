package com.gtkim.mobile_access_control.core.database.master.dao

import androidx.room.Dao
import androidx.room.Query
import com.gtkim.mobile_access_control.core.database.master.entity.ZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ZoneDao {
    @Query("SELECT * FROM zones ORDER BY code ASC")
    fun observeAll(): Flow<List<ZoneEntity>>
}
