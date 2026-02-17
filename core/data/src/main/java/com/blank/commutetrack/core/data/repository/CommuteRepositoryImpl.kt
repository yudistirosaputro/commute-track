package com.blank.commutetrack.core.data.repository

import com.blank.commutetrack.core.data.mapper.toDomain
import com.blank.commutetrack.core.data.mapper.toEntity
import com.blank.commutetrack.core.database.dao.CommuteSessionDao
import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.model.CommuteStatistics
import com.blank.commutetrack.core.domain.model.DailyTrips
import com.blank.commutetrack.core.domain.model.SessionStatus
import com.blank.commutetrack.core.domain.model.TransportMode
import com.blank.commutetrack.core.domain.repository.CommuteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommuteRepositoryImpl @Inject constructor(
    private val dao: CommuteSessionDao
) : CommuteRepository {

    override fun getActiveSessions(): Flow<List<CommuteSession>> =
        dao.getActiveSessions().map { entities -> entities.map { it.toDomain() } }

    override fun getSessionHistory(): Flow<List<CommuteSession>> =
        dao.getSessionHistory().map { entities -> entities.map { it.toDomain() } }

    override fun getSessionsByDate(date: LocalDate): Flow<List<CommuteSession>> =
        dao.getSessionsByDate(date.toString()).map { entities -> entities.map { it.toDomain() } }

    override fun getSessionById(id: Long): Flow<CommuteSession?> =
        dao.getSessionById(id).map { it?.toDomain() }

    override suspend fun startSession(session: CommuteSession): Long =
        dao.insertSession(session.toEntity())

    override suspend fun updateSession(session: CommuteSession) =
        dao.updateSession(session.toEntity())

    override suspend fun endSession(id: Long, endLocation: String, distanceKm: Double) {
        val entity = dao.getSessionById(id).first()
        entity?.let {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val startDateTime = kotlinx.datetime.LocalDateTime.parse(it.startTime)

            // Calculate duration handling day boundaries
            val daysDiff = now.date.toEpochDays() - startDateTime.date.toEpochDays()
            val timeDiffMinutes = (now.hour * 60 + now.minute) - (startDateTime.hour * 60 + startDateTime.minute)
            val durationMinutes = (daysDiff * 24 * 60 + timeDiffMinutes).coerceAtLeast(0)

            dao.updateSession(
                it.copy(
                    endTime = now.toString(),
                    endLocation = endLocation,
                    distanceKm = distanceKm,
                    durationMinutes = durationMinutes,
                    status = SessionStatus.COMPLETED.name
                )
            )
        }
    }

    override suspend fun deleteSession(id: Long) = dao.deleteSession(id)

    override fun getStatistics(): Flow<CommuteStatistics> =
        dao.getAllCompletedSessions().map { entities ->
            val sessions = entities.map { it.toDomain() }
            calculateStatistics(sessions)
        }

    override fun getStatisticsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<CommuteStatistics> =
        dao.getSessionsByDateRange(startDate.toString(), endDate.toString()).map { entities ->
            val sessions = entities.map { it.toDomain() }
            calculateStatistics(sessions)
        }

    private fun calculateStatistics(sessions: List<CommuteSession>): CommuteStatistics {
        if (sessions.isEmpty()) return CommuteStatistics()

        val totalTrips = sessions.size
        val totalDistance = sessions.sumOf { it.distanceKm }
        val totalDuration = sessions.sumOf { it.durationMinutes }
        val transportCounts = sessions.groupBy { it.transportMode }
        val mostUsed = transportCounts.maxByOrNull { it.value.size }?.key

        val dailyTrips = sessions.groupBy { it.date.dayOfWeek.name }
            .map { (day, trips) ->
                DailyTrips(
                    dayOfWeek = day.take(3).lowercase().replaceFirstChar { it.uppercase() },
                    tripCount = trips.size,
                    totalDistanceKm = trips.sumOf { it.distanceKm }
                )
            }

        return CommuteStatistics(
            totalTrips = totalTrips,
            totalDistanceKm = totalDistance,
            totalDurationMinutes = totalDuration,
            averageDurationMinutes = if (totalTrips > 0) totalDuration / totalTrips else 0,
            averageDistanceKm = if (totalTrips > 0) totalDistance / totalTrips else 0.0,
            mostUsedTransport = mostUsed,
            weeklyTrips = dailyTrips
        )
    }
}
