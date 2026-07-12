package com.gtkim.mobile_access_control.core.database.master.dao

import androidx.room.Dao
import androidx.room.Query
import com.gtkim.mobile_access_control.core.database.master.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun byId(id: Long): UserEntity?
}
