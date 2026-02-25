package com.blank.commutetrack.feature.statistics

import com.blank.commutetrack.core.domain.model.CommuteStatistics
import com.blank.commutetrack.core.domain.model.DepartureTimeStats
import com.blank.commutetrack.core.domain.model.TransportMode

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val statistics: CommuteStatistics = CommuteStatistics(),
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEK,
    val transportBreakdown: Map<TransportMode, Int> = emptyMap(),
    val departureTimeAnalysis: List<DepartureTimeStats> = emptyList(),
    val bestDepartureTime: DepartureTimeStats? = null,
    val worstDepartureTime: DepartureTimeStats? = null
)

enum class StatsPeriod { WEEK, MONTH, YEAR }
