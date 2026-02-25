package com.blank.commutetrack.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blank.commutetrack.core.domain.usecase.GetDepartureTimeAnalysisUseCase
import com.blank.commutetrack.core.domain.usecase.GetStatisticsUseCase
import com.blank.commutetrack.core.domain.usecase.GetSessionHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getStatistics: GetStatisticsUseCase,
    private val getSessionHistory: GetSessionHistoryUseCase,
    private val getDepartureTimeAnalysis: GetDepartureTimeAnalysisUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            getStatistics().collect { stats ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statistics = stats,
                        departureTimeAnalysis = stats.departureTimeAnalysis,
                        bestDepartureTime = stats.bestDepartureTime,
                        worstDepartureTime = stats.worstDepartureTime
                    )
                }
            }
        }
        viewModelScope.launch {
            getSessionHistory().collect { sessions ->
                val breakdown = sessions.groupBy { it.transportMode }
                    .mapValues { it.value.size }
                _uiState.update { it.copy(transportBreakdown = breakdown) }
            }
        }
    }

    fun selectPeriod(period: StatsPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }
}
