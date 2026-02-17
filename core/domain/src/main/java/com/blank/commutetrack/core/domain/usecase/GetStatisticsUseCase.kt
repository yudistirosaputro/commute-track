package com.blank.commutetrack.core.domain.usecase

import com.blank.commutetrack.core.domain.model.CommuteStatistics
import com.blank.commutetrack.core.domain.repository.CommuteRepository
import kotlinx.coroutines.flow.Flow

class GetStatisticsUseCase(
    private val repository: CommuteRepository
) {
    operator fun invoke(): Flow<CommuteStatistics> =
        repository.getStatistics()
}
