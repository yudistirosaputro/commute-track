package com.blank.commutetrack.core.domain.usecase

import com.blank.commutetrack.core.domain.model.DepartureTimeStats
import com.blank.commutetrack.core.domain.repository.CommuteRepository

class GetDepartureTimeAnalysisUseCase(
    private val repository: CommuteRepository
) {
    suspend operator fun invoke(): List<DepartureTimeStats> =
        repository.getDepartureTimeAnalysis()
}
