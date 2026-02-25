package com.blank.commutetrack.feature.history

import com.blank.commutetrack.core.domain.model.CommuteSession
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

data class HistoryUiState(
    val isLoading: Boolean = true,
    val allSessions: List<CommuteSession> = emptyList(),
    val filterPeriod: FilterPeriod = FilterPeriod.ALL
) {
    /**
     * Filtered sessions based on selected period
     */
    val filteredSessions: List<CommuteSession>
        get() = when (filterPeriod) {
            FilterPeriod.ALL -> allSessions
            FilterPeriod.WEEK -> allSessions.filter { it.isFromThisWeek() }
            FilterPeriod.MONTH -> allSessions.filter { it.isFromThisMonth() }
            FilterPeriod.YEAR -> allSessions.filter { it.isFromThisYear() }
        }
}

enum class FilterPeriod { ALL, WEEK, MONTH, YEAR }

private fun CommuteSession.isFromThisWeek(): Boolean {
    val now = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val today = now.date
    val dayOfWeek = today.dayOfWeek.value // 1 = Monday, 7 = Sunday
    val daysFromMonday = dayOfWeek - 1
    val weekStart = today.minus(kotlinx.datetime.DatePeriod(days = daysFromMonday))
    return date >= weekStart
}

private fun CommuteSession.isFromThisMonth(): Boolean {
    val now = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return date.year == now.year && date.month == now.month
}

private fun CommuteSession.isFromThisYear(): Boolean {
    val now = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return date.year == now.year
}
