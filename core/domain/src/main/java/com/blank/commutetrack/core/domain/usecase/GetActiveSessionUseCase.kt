package com.blank.commutetrack.core.domain.usecase

import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.repository.CommuteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetActiveSessionUseCase(
    private val repository: CommuteRepository
) {
    operator fun invoke(): Flow<CommuteSession?> =
        repository.getActiveSessions().map { it.firstOrNull() }
}
