package com.blank.commutetrack.core.domain.usecase

import com.blank.commutetrack.core.domain.repository.CommuteRepository

class EndSessionUseCase(
    private val repository: CommuteRepository
) {
    suspend operator fun invoke(sessionId: Long, endLocation: String, distanceKm: Double) {
        repository.endSession(sessionId, endLocation, distanceKm)
    }
}
