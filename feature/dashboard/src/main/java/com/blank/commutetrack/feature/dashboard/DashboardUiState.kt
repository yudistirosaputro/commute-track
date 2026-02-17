package com.blank.commutetrack.feature.dashboard

import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.model.CommuteStatistics

data class DashboardUiState(
    val isLoading: Boolean = true,
    val activeSession: CommuteSession? = null,
    val recentTrips: List<CommuteSession> = emptyList(),
    val todayStats: TodayStats = TodayStats(),
    val weeklyStats: CommuteStatistics = CommuteStatistics()
)

data class TodayStats(
    val tripsCount: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val totalDurationMinutes: Int = 0
)
