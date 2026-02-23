package com.blank.commutetrack.feature.settings

import com.blank.commutetrack.core.domain.model.UserSettings

data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: UserSettings = UserSettings(),
    val exportedCsv: String? = null,
    val isGeneratingData: Boolean = false,
    val showDummyDataSuccess: Boolean = false,
    val errorMessage: String? = null
)
