package com.blank.commutetrack.feature.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.model.SessionStatus
import com.blank.commutetrack.core.domain.model.TransportMode
import com.blank.commutetrack.core.domain.usecase.EndSessionUseCase
import com.blank.commutetrack.core.domain.usecase.GetActiveSessionUseCase
import com.blank.commutetrack.core.domain.usecase.PauseSessionUseCase
import com.blank.commutetrack.core.domain.usecase.ResumeSessionUseCase
import com.blank.commutetrack.core.domain.usecase.StartSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val endSession: EndSessionUseCase,
    private val pauseSession: PauseSessionUseCase,
    private val resumeSession: ResumeSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private var elapsedTimeJob: Job? = null
    private var pauseStartTime: kotlinx.datetime.LocalDateTime? = null

    init {
        loadActiveSession()
    }

    private fun loadActiveSession() {
        viewModelScope.launch {
            getActiveSession().collect { session ->
                val isPaused = session?.status == SessionStatus.PAUSED
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeSession = session,
                        isTracking = session != null,
                        isPaused = isPaused,
                        startLocation = session?.startLocation ?: it.startLocation,
                        selectedTransportMode = session?.transportMode ?: it.selectedTransportMode,
                        pauseCount = session?.pauseCount ?: 0,
                        pausedMinutes = session?.pausedMinutes ?: 0
                    )
                }
                if (session != null && !isPaused) {
                    startElapsedTimeTracking(session)
                } else if (session != null && isPaused) {
                    // Show frozen time when paused
                    updateElapsedTimeDisplay(session)
                    elapsedTimeJob?.cancel()
                } else {
                    elapsedTimeJob?.cancel()
                    _uiState.update { it.copy(elapsedTime = "00:00") }
                }
            }
        }
    }

    private fun startElapsedTimeTracking(session: CommuteSession) {
        elapsedTimeJob?.cancel()
        elapsedTimeJob = viewModelScope.launch {
            while (true) {
                updateElapsedTimeDisplay(session)
                delay(10_000) // 10-second intervals for battery efficiency
            }
        }
    }

    private fun updateElapsedTimeDisplay(session: CommuteSession) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val start = session.startTime

        val startMinutes = start.hour * 60 + start.minute
        val nowMinutes = now.hour * 60 + now.minute
        val daysDiff = now.date.toEpochDays() - start.date.toEpochDays()
        val totalMinutes = (daysDiff * 24 * 60 + nowMinutes - startMinutes).coerceAtLeast(0)

        val activeMinutes = (totalMinutes - _uiState.value.pausedMinutes).coerceAtLeast(0)
        _uiState.update { it.copy(elapsedTime = activeMinutes.formatDuration()) }
    }

    fun updateStartLocation(location: String) {
        _uiState.update { it.copy(startLocation = location) }
    }

    fun selectTransportMode(mode: TransportMode) {
        _uiState.update { it.copy(selectedTransportMode = mode) }
    }

    fun startTrip() {
        val location = _uiState.value.startLocation
        if (location.isBlank()) return
        viewModelScope.launch {
            startSession(location, _uiState.value.selectedTransportMode)
        }
    }

    fun pauseTrip() {
        viewModelScope.launch {
            _uiState.value.activeSession?.let { session ->
                pauseStartTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                pauseSession(session.id)
                elapsedTimeJob?.cancel()
                _uiState.update { it.copy(isPaused = true) }
            }
        }
    }

    fun resumeTrip() {
        viewModelScope.launch {
            _uiState.value.activeSession?.let { session ->
                val pauseEnd = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val pauseStart = pauseStartTime ?: pauseEnd

                val pauseStartMin = pauseStart.hour * 60 + pauseStart.minute
                val pauseEndMin = pauseEnd.hour * 60 + pauseEnd.minute
                val daysDiff = pauseEnd.date.toEpochDays() - pauseStart.date.toEpochDays()
                val pauseDuration = (daysDiff * 24 * 60 + pauseEndMin - pauseStartMin).coerceAtLeast(0)

                resumeSession(session.id, pauseDuration)
                pauseStartTime = null

                _uiState.update {
                    it.copy(
                        isPaused = false,
                        pauseCount = it.pauseCount + 1,
                        pausedMinutes = it.pausedMinutes + pauseDuration
                    )
                }
            }
        }
    }

    fun endTrip(endLocation: String, distanceKm: Double) {
        viewModelScope.launch {
            _uiState.value.activeSession?.let { session ->
                // If paused, resume first to account for final pause duration
                if (_uiState.value.isPaused) {
                    val pauseEnd = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val pauseStart = pauseStartTime ?: pauseEnd
                    val pauseStartMin = pauseStart.hour * 60 + pauseStart.minute
                    val pauseEndMin = pauseEnd.hour * 60 + pauseEnd.minute
                    val daysDiff = pauseEnd.date.toEpochDays() - pauseStart.date.toEpochDays()
                    val pauseDuration = (daysDiff * 24 * 60 + pauseEndMin - pauseStartMin).coerceAtLeast(0)
                    resumeSession(session.id, pauseDuration)
                    pauseStartTime = null
                }
                endSession(session.id, endLocation, distanceKm)
            }
        }
    }

    private fun Int.formatDuration(): String {
        val hours = this / 60
        val minutes = this % 60
        return when {
            hours > 0 -> "%d:%02d".format(hours, minutes)
            else -> "00:%02d".format(minutes)
        }
    }
}
