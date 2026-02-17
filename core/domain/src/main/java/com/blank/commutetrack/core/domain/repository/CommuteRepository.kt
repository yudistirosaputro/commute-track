package com.blank.commutetrack.core.domain.repository

import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.model.CommuteStatistics
import com.blank.commutetrack.core.domain.model.TransportMode
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface CommuteRepository {
    fun getActiveSessions(): Flow<List<CommuteSession>>
    fun getSessionHistory(): Flow<List<CommuteSession>>
    fun getSessionsByDate(date: LocalDate): Flow<List<CommuteSession>>
    fun getSessionById(id: Long): Flow<CommuteSession?>
    suspend fun startSession(session: CommuteSession): Long
    suspend fun updateSession(session: CommuteSession)
    suspend fun endSession(id: Long, endLocation: String, distanceKm: Double)
    suspend fun deleteSession(id: Long)
    fun getStatistics(): Flow<CommuteStatistics>
    fun getStatisticsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<CommuteStatistics>
}
