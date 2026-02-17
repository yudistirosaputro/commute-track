package com.blank.commutetrack.core.domain.model

data class CommuteStatistics(
    val totalTrips: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val totalDurationMinutes: Int = 0,
    val averageDurationMinutes: Int = 0,
    val averageDistanceKm: Double = 0.0,
    val mostUsedTransport: TransportMode? = null,
    val weeklyTrips: List<DailyTrips> = emptyList(),
    val monthlyDistanceKm: Double = 0.0,
    val streakDays: Int = 0
)

data class DailyTrips(
    val dayOfWeek: String,
    val tripCount: Int,
    val totalDistanceKm: Double
)
