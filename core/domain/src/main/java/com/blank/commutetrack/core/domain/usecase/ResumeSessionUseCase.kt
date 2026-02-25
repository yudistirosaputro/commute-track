package com.blank.commutetrack.core.domain.usecase

import com.blank.commutetrack.core.domain.repository.CommuteRepository

class ResumeSessionUseCase(
    private val repository: CommuteRepository
) {
    suspend operator fun invoke(sessionId: Long, pauseDurationMinutes: Int) {
        repository.resumeSession(sessionId, pauseDurationMinutes)
    }
}
