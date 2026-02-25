package com.blank.commutetrack.core.domain.usecase

import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.repository.CommuteRepository

class ExportDataUseCase(
    private val repository: CommuteRepository
) {
    suspend operator fun invoke(): List<CommuteSession> =
        repository.getAllCompletedSessionsList()
}
