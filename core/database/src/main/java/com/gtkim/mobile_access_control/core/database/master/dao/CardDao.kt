package com.gtkim.mobile_access_control.core.database.master.dao

import androidx.room.Dao
import androidx.room.Query
import com.gtkim.mobile_access_control.core.database.master.entity.CardEntity

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE cardUid = :uid LIMIT 1")
    suspend fun byUid(uid: String): CardEntity?
}
