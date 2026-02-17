package com.blank.commutetrack.core.domain.usecase

import com.blank.commutetrack.core.domain.model.SessionStatus
import com.blank.commutetrack.core.domain.repository.CommuteRepository

class PauseSessionUseCase(
    private val repository: CommuteRepository
) {
    suspend operator fun invoke(sessionId: Long) {
        repository.updateSessionStatus(sessionId, SessionStatus.PAUSED.name)
    }
}
