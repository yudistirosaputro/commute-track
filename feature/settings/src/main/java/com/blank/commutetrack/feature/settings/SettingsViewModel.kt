package com.blank.commutetrack.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blank.commutetrack.core.domain.model.DistanceUnit
import com.blank.commutetrack.core.domain.model.TransportMode
import com.blank.commutetrack.core.domain.model.UserSettings
import com.blank.commutetrack.core.domain.usecase.ExportDataUseCase
import com.blank.commutetrack.core.domain.usecase.GenerateDummyDataUseCase
import com.blank.commutetrack.core.domain.usecase.GetSettingsUseCase
import com.blank.commutetrack.core.domain.usecase.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettings: GetSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase,
    private val exportDataUseCase: ExportDataUseCase,
    private val generateDummyDataUseCase: GenerateDummyDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            getSettings().collect { settings ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        settings = settings
                    )
                }
            }
        }
    }

    fun updateDefaultTransport(mode: TransportMode) {
        val current = _uiState.value.settings
        viewModelScope.launch {
            updateSettings(current.copy(defaultTransportMode = mode))
        }
    }

    fun updateDistanceUnit(unit: DistanceUnit) {
        val current = _uiState.value.settings
        viewModelScope.launch {
            updateSettings(current.copy(distanceUnit = unit))
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        val current = _uiState.value.settings
        viewModelScope.launch {
            updateSettings(current.copy(notificationsEnabled = enabled))
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        val current = _uiState.value.settings
        viewModelScope.launch {
            updateSettings(current.copy(darkModeEnabled = enabled))
        }
    }

    fun toggleAutoStart(enabled: Boolean) {
        val current = _uiState.value.settings
        viewModelScope.launch {
            updateSettings(current.copy(autoStartEnabled = enabled))
        }
    }

    fun updateHomeAddress(address: String) {
        val current = _uiState.value.settings
        viewModelScope.launch {
            updateSettings(current.copy(homeAddress = address))
        }
    }

    fun updateWorkAddress(address: String) {
        val current = _uiState.value.settings
        viewModelScope.launch {
            updateSettings(current.copy(workAddress = address))
        }
    }

    fun exportData() {
        viewModelScope.launch {
            val sessions = exportDataUseCase()
            val csvData = buildString {
                appendLine("date,day_of_week,departure_time,arrival_time,duration_minutes,paused_minutes,pause_count,distance_km,avg_speed_kmh,transport_mode,start_location,end_location")
                sessions.forEach { session ->
                    appendLine(
                        "${session.date}," +
                        "${session.date.dayOfWeek}," +
                        "${session.startTime.hour}:${session.startTime.minute.toString().padStart(2, '0')}," +
                        "${session.endTime?.let { "${it.hour}:${it.minute.toString().padStart(2, '0')}" } ?: ""}," +
                        "${session.durationMinutes}," +
                        "${session.pausedMinutes}," +
                        "${session.pauseCount}," +
                        "${session.distanceKm}," +
                        "${"%.1f".format(session.averageSpeedKmh)}," +
                        "${session.transportMode}," +
                        "\"${session.startLocation}\"," +
                        "\"${session.endLocation}\""
                    )
                }
            }
            _uiState.update { it.copy(exportedCsv = csvData) }
        }
    }

    /**
     * Generate dummy commute data for testing purposes.
     * Creates 20-30 random trips from the past 30 days.
     */
    fun generateDummyData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingData = true) }
            try {
                generateDummyDataUseCase()
                _uiState.update { it.copy(
                    isGeneratingData = false,
                    showDummyDataSuccess = true
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isGeneratingData = false,
                    errorMessage = e.message ?: "Failed to generate dummy data"
                ) }
            }
        }
    }

    fun dismissSuccessMessage() {
        _uiState.update { it.copy(showDummyDataSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
