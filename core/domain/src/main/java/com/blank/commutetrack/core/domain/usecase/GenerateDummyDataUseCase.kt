package com.blank.commutetrack.core.domain.usecase

import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.model.SessionStatus
import com.blank.commutetrack.core.domain.model.TransportMode
import com.blank.commutetrack.core.domain.repository.CommuteRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

/**
 * Use case to generate dummy commute data for testing purposes.
 */
class GenerateDummyDataUseCase(
    private val repository: CommuteRepository
) {
    suspend operator fun invoke() {
        // Generate trips for the past 30 days
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        
        // Sample locations
        val homeLocations = listOf("Home", "Apartment", "House", "Condo")
        val workLocations = listOf("Office", "Work", "Downtown", "City Center", "Tech Park")
        val otherLocations = listOf("Gym", "Mall", "Supermarket", "Cafe", "Library", "Restaurant")
        
        // Generate 20-30 random trips
        val tripCount = Random.nextInt(20, 31)
        
        repeat(tripCount) { index ->
            // Random date within past 30 days, weighted toward recent days
            val daysAgo = Random.nextInt(0, 31)
            val tripDate = today.minus(DatePeriod(days = daysAgo))
            
            // Determine trip type (home-work, work-home, or other)
            val tripType = Random.nextInt(0, 10)
            val (startLoc, endLoc) = when {
                tripType < 4 -> homeLocations.random() to workLocations.random() // Morning commute
                tripType < 7 -> workLocations.random() to homeLocations.random() // Evening commute
                else -> (homeLocations + workLocations).random() to otherLocations.random() // Other trip
            }
            
            // Random transport mode with realistic distribution
            val transportMode = when (Random.nextInt(0, 100)) {
                in 0..30 -> TransportMode.PUBLIC_TRANSIT
                in 31..50 -> TransportMode.DRIVING
                in 51..70 -> TransportMode.WALKING
                in 71..85 -> TransportMode.CYCLING
                else -> TransportMode.MOTORCYCLE
            }
            
            // Random start time (morning 7-9, evening 17-19, or other times)
            val (startHour, startMinute) = when {
                tripType < 4 -> Pair(Random.nextInt(7, 10), Random.nextInt(0, 60)) // Morning
                tripType < 7 -> Pair(Random.nextInt(17, 20), Random.nextInt(0, 60)) // Evening
                else -> Pair(Random.nextInt(6, 22), Random.nextInt(0, 60)) // Other
            }
            
            val startTime = LocalDateTime(
                date = tripDate,
                time = LocalTime(startHour, startMinute, 0)
            )
            
            // Duration based on transport mode and random variation
            val baseDuration = when (transportMode) {
                TransportMode.WALKING -> Random.nextInt(15, 45)
                TransportMode.CYCLING -> Random.nextInt(10, 30)
                TransportMode.DRIVING -> Random.nextInt(15, 60)
                TransportMode.PUBLIC_TRANSIT -> Random.nextInt(20, 70)
                TransportMode.MOTORCYCLE -> Random.nextInt(10, 40)
            }
            val durationMinutes = baseDuration + Random.nextInt(-5, 10)
            
            // Distance based on duration and transport mode
            val averageSpeed = when (transportMode) {
                TransportMode.WALKING -> 5.0
                TransportMode.CYCLING -> 15.0
                TransportMode.DRIVING -> 25.0
                TransportMode.PUBLIC_TRANSIT -> 20.0
                TransportMode.MOTORCYCLE -> 30.0
            }
            val distanceKm = (durationMinutes / 60.0) * averageSpeed * (0.8 + Random.nextDouble(0.4))
            
            // Calculate end time
            val endHour = startHour + (durationMinutes / 60)
            val endMinute = startMinute + (durationMinutes % 60)
            val adjustedEndHour = endHour + (endMinute / 60)
            val adjustedEndMinute = endMinute % 60
            
            val endTime = LocalDateTime(
                date = if (adjustedEndHour >= 24) tripDate.plus(DatePeriod(days = 1)) else tripDate,
                time = LocalTime(adjustedEndHour % 24, adjustedEndMinute % 60, 0)
            )
            
            // Random pauses (some trips have pauses, most don't)
            val pauseCount = when (Random.nextInt(0, 10)) {
                0 -> Random.nextInt(1, 3) // 10% chance of 1-2 pauses
                else -> 0
            }
            val pausedMinutes = if (pauseCount > 0) Random.nextInt(2, 15) * pauseCount else 0
            
            // Actual duration (excluding pauses)
            val actualDuration = durationMinutes - pausedMinutes
            
            // Create and save the session
            val session = CommuteSession(
                id = 0, // Will be auto-generated
                startTime = startTime,
                endTime = endTime,
                startLocation = startLoc,
                endLocation = endLoc,
                transportMode = transportMode,
                distanceKm = distanceKm,
                durationMinutes = actualDuration.coerceAtLeast(5),
                status = SessionStatus.COMPLETED,
                date = tripDate,
                notes = "",
                pausedMinutes = pausedMinutes,
                pauseCount = pauseCount,
                averageSpeedKmh = if (actualDuration > 0) (distanceKm / (actualDuration / 60.0)) else 0.0
            )
            
            repository.startSession(session)
        }
        
        // Create one active session (for testing active trip UI)
        if (Random.nextBoolean()) {
            val startMinutesAgo = Random.nextInt(5, 60)
            val startHour = now.time.hour
            val startMinute = now.time.minute - startMinutesAgo
            val adjustedStartHour = if (startMinute < 0) startHour - 1 else startHour
            val adjustedStartMinute = if (startMinute < 0) startMinute + 60 else startMinute
            
            val activeSession = CommuteSession(
                id = 0,
                startTime = LocalDateTime(
                    date = if (adjustedStartHour < 0) today.minus(DatePeriod(days = 1)) else today,
                    time = LocalTime((adjustedStartHour + 24) % 24, adjustedStartMinute, 0)
                ),
                endTime = null,
                startLocation = homeLocations.random(),
                endLocation = "",
                transportMode = TransportMode.PUBLIC_TRANSIT,
                distanceKm = 0.0,
                durationMinutes = 0,
                status = SessionStatus.ACTIVE,
                date = today,
                pausedMinutes = 0,
                pauseCount = 0,
                averageSpeedKmh = 0.0
            )
            repository.startSession(activeSession)
        }
    }
}
