package com.blank.commutetrack.core.domain.usecase

import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.repository.CommuteRepository
import kotlinx.coroutines.flow.Flow

class GetSessionHistoryUseCase(
    private val repository: CommuteRepository
) {
    operator fun invoke(): Flow<List<CommuteSession>> =
        repository.getSessionHistory()
}
