package com.gtkim.mobile_access_control.core.database.sync.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gtkim.mobile_access_control.core.database.sync.entity.PendingLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingLogDao {
    @Query("SELECT * FROM pending_logs ORDER BY decidedAtEpochMs ASC")
    suspend fun all(): List<PendingLogEntity>

    @Query("SELECT COUNT(*) FROM pending_logs")
    fun count(): Flow<Int>

    /** enqueue 시 큐 limit 체크용 — Flow 가 아닌 즉시 값. */
    @Query("SELECT COUNT(*) FROM pending_logs")
    suspend fun countNow(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PendingLogEntity)

    @Query("DELETE FROM pending_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * 가장 오래된 [n] 건을 삭제 — 큐 overflow 시 호출. decidedAt 오름차순 = 오래된 순.
     */
    @Query(
        """
        DELETE FROM pending_logs
        WHERE id IN (
            SELECT id FROM pending_logs ORDER BY decidedAtEpochMs ASC LIMIT :n
        )
        """,
    )
    suspend fun deleteOldest(n: Int)

    @Query("UPDATE pending_logs SET attempts = attempts + 1 WHERE id = :id")
    suspend fun incrementAttempts(id: String)

    @Query("SELECT attempts FROM pending_logs WHERE id = :id")
    suspend fun attemptsOf(id: String): Int?

    /**
     * attempts 를 1 올리고 결과값을 반환. dead-letter 판정 (>= MAX) 에 사용. 단일 트랜잭션으로
     * 묶어 다른 Worker/세션이 중간에 같은 row 를 건드릴 가능성을 차단한다 (현재 unique work +
     * KEEP 로 동시 실행은 없지만 방어적).
     */
    @Transaction
    suspend fun incrementAndGetAttempts(id: String): Int? {
        incrementAttempts(id)
        return attemptsOf(id)
    }
}
