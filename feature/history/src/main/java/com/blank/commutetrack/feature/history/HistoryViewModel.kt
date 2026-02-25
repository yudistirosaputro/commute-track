package com.blank.commutetrack.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blank.commutetrack.core.domain.usecase.GetSessionHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getSessionHistory: GetSessionHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getSessionHistory().collect { sessions ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allSessions = sessions
                    )
                }
            }
        }
    }

    fun setFilterPeriod(period: FilterPeriod) {
        _uiState.update { it.copy(filterPeriod = period) }
    }
}
