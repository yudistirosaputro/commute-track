package com.blank.commutetrack.feature.history

import com.blank.commutetrack.core.domain.model.CommuteSession

data class HistoryUiState(
    val isLoading: Boolean = true,
    val sessions: List<CommuteSession> = emptyList(),
    val filterPeriod: FilterPeriod = FilterPeriod.ALL
)

enum class FilterPeriod { ALL, WEEK, MONTH, YEAR }
