package com.blank.commutetrack.feature.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.model.TransportMode
import com.blank.commutetrack.core.domain.usecase.EndSessionUseCase
import com.blank.commutetrack.core.domain.usecase.GetActiveSessionUseCase
import com.blank.commutetrack.core.domain.usecase.StartSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val getActiveSession: GetActiveSessionUseCase,
    private val startSession: StartSessionUseCase,
    private val endSession: EndSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private var elapsedTimeJob: kotlinx.coroutines.Job? = null

    init {
        loadActiveSession()
    }

    private fun loadActiveSession() {
        viewModelScope.launch {
            getActiveSession().collect { session ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeSession = session,
                        isTracking = session != null,
                        startLocation = session?.startLocation ?: "",
                        selectedTransportMode = session?.transportMode ?: TransportMode.DRIVING
                    )
                }
                if (session != null) {
                    startElapsedTimeTracking(session)
                } else {
                    elapsedTimeJob?.cancel()
                    _uiState.update { it.copy(elapsedTime = "0m") }
                }
            }
        }
    }

    private fun startElapsedTimeTracking(session: CommuteSession) {
        elapsedTimeJob?.cancel()
        elapsedTimeJob = viewModelScope.launch {
            while (true) {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val start = session.startTime
                val minutes = ((now.hour * 60 + now.minute) - (start.hour * 60 + start.minute)).coerceAtLeast(0)
                _uiState.update { it.copy(elapsedTime = minutes.formatDuration()) }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun updateStartLocation(location: String) {
        _uiState.update { it.copy(startLocation = location) }
    }

    fun selectTransportMode(mode: TransportMode) {
        _uiState.update { it.copy(selectedTransportMode = mode) }
    }

    fun startTrip() {
        viewModelScope.launch {
            startSession(_uiState.value.startLocation, _uiState.value.selectedTransportMode)
        }
    }

    fun endTrip(endLocation: String, distanceKm: Double) {
        viewModelScope.launch {
            _uiState.value.activeSession?.let { session ->
                endSession(session.id, endLocation, distanceKm)
            }
        }
    }

    private fun Int.formatDuration(): String {
        val hours = this / 60
        val minutes = this % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
}
