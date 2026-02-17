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
    val streakDays: Int = 0,
    val departureTimeAnalysis: List<DepartureTimeStats> = emptyList(),
    val bestDepartureTime: DepartureTimeStats? = null,
    val worstDepartureTime: DepartureTimeStats? = null
)

data class DailyTrips(
    val dayOfWeek: String,
    val tripCount: Int,
    val totalDistanceKm: Double
)

data class DepartureTimeStats(
    val hourOfDay: Int,
    val tripCount: Int,
    val averageDurationMinutes: Int,
    val averageSpeedKmh: Double,
    val averagePauseCount: Int,
    val dayOfWeekBreakdown: Map<String, Int> = emptyMap()
)
