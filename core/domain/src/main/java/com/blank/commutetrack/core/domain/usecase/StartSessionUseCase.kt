package com.blank.commutetrack.core.domain.usecase

import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.model.TransportMode
import com.blank.commutetrack.core.domain.repository.CommuteRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class StartSessionUseCase(
    private val repository: CommuteRepository
) {
    suspend operator fun invoke(
        startLocation: String,
        transportMode: TransportMode
    ): Long {
        val session = CommuteSession(
            startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            startLocation = startLocation,
            transportMode = transportMode
        )
        return repository.startSession(session)
    }
}
