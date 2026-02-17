package com.blank.commutetrack.core.domain.usecase

import com.blank.commutetrack.core.domain.model.UserSettings
import com.blank.commutetrack.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<UserSettings> =
        repository.getSettings()
}
