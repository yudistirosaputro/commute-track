package com.blank.commutetrack.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blank.commutetrack.core.domain.model.DistanceUnit
import com.blank.commutetrack.core.domain.model.TransportMode
import com.blank.commutetrack.core.domain.model.UserSettings
import com.blank.commutetrack.core.domain.usecase.GetSettingsUseCase
import com.blank.commutetrack.core.domain.usecase.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettings: GetSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase
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
}
