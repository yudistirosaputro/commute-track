package com.blank.commutetrack.core.database.dao

import androidx.room.*
import com.blank.commutetrack.core.database.entity.CommuteSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommuteSessionDao {
    @Query("SELECT * FROM commute_sessions WHERE status IN ('ACTIVE', 'PAUSED') ORDER BY startTime DESC")
    fun getActiveSessions(): Flow<List<CommuteSessionEntity>>

    @Query("SELECT * FROM commute_sessions WHERE status = 'COMPLETED' ORDER BY startTime DESC")
    fun getSessionHistory(): Flow<List<CommuteSessionEntity>>

    @Query("SELECT * FROM commute_sessions WHERE date = :date ORDER BY startTime DESC")
    fun getSessionsByDate(date: String): Flow<List<CommuteSessionEntity>>

    @Query("SELECT * FROM commute_sessions WHERE id = :id")
    fun getSessionById(id: Long): Flow<CommuteSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CommuteSessionEntity): Long

    @Update
    suspend fun updateSession(session: CommuteSessionEntity)

    @Query("DELETE FROM commute_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("SELECT * FROM commute_sessions WHERE status = 'COMPLETED' ORDER BY startTime DESC")
    fun getAllCompletedSessions(): Flow<List<CommuteSessionEntity>>

    @Query("SELECT * FROM commute_sessions WHERE date BETWEEN :startDate AND :endDate AND status = 'COMPLETED' ORDER BY startTime DESC")
    fun getSessionsByDateRange(startDate: String, endDate: String): Flow<List<CommuteSessionEntity>>

    @Query("SELECT COUNT(*) FROM commute_sessions WHERE status = 'COMPLETED'")
    fun getTotalCompletedTrips(): Flow<Int>

    @Query("SELECT * FROM commute_sessions WHERE status IN ('ACTIVE', 'PAUSED') ORDER BY startTime DESC")
    fun getActiveOrPausedSessions(): Flow<List<CommuteSessionEntity>>

    @Query("UPDATE commute_sessions SET status = :status WHERE id = :id")
    suspend fun updateSessionStatus(id: Long, status: String)

    @Query("UPDATE commute_sessions SET status = 'ACTIVE', pausedMinutes = pausedMinutes + :additionalPausedMinutes, pauseCount = pauseCount + 1 WHERE id = :id")
    suspend fun resumeSession(id: Long, additionalPausedMinutes: Int)

    @Query("SELECT * FROM commute_sessions WHERE status = 'COMPLETED' ORDER BY startTime DESC")
    suspend fun getAllCompletedSessionsList(): List<CommuteSessionEntity>
}
