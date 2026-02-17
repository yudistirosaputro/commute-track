package com.blank.commutetrack.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blank.commutetrack.core.domain.usecase.GetActiveSessionUseCase
import com.blank.commutetrack.core.domain.usecase.GetSessionHistoryUseCase
import com.blank.commutetrack.core.domain.usecase.GetStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getActiveSession: GetActiveSessionUseCase,
    private val getSessionHistory: GetSessionHistoryUseCase,
    private val getStatistics: GetStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            getActiveSession().collect { session ->
                _uiState.update { it.copy(activeSession = session) }
            }
        }
        viewModelScope.launch {
            getSessionHistory().collect { sessions ->
                val recentTrips = sessions.take(5)
                val todayTrips = sessions.filter {
                    it.date == kotlinx.datetime.Clock.System.now()
                        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        recentTrips = recentTrips,
                        todayStats = TodayStats(
                            tripsCount = todayTrips.size,
                            totalDistanceKm = todayTrips.sumOf { t -> t.distanceKm },
                            totalDurationMinutes = todayTrips.sumOf { t -> t.durationMinutes }
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            getStatistics().collect { stats ->
                _uiState.update { it.copy(weeklyStats = stats) }
            }
        }
    }
}
