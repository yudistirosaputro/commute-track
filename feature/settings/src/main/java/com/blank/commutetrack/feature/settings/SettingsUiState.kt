package com.blank.commutetrack.feature.settings

import com.blank.commutetrack.core.domain.model.DistanceUnit
import com.blank.commutetrack.core.domain.model.TransportMode
import com.blank.commutetrack.core.domain.model.UserSettings

data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: UserSettings = UserSettings()
)
