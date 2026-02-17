package com.blank.commutetrack.core.domain.usecase

import com.blank.commutetrack.core.domain.model.UserSettings
import com.blank.commutetrack.core.domain.repository.SettingsRepository

class UpdateSettingsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(settings: UserSettings) {
        repository.updateSettings(settings)
    }
}
