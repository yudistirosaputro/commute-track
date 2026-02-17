package com.blank.commutetrack.core.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalDate

data class CommuteSession(
    val id: Long = 0,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val startLocation: String,
    val endLocation: String = "",
    val transportMode: TransportMode,
    val distanceKm: Double = 0.0,
    val durationMinutes: Int = 0,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val date: LocalDate = startTime.date,
    val notes: String = ""
)

enum class TransportMode {
    WALKING, CYCLING, DRIVING, PUBLIC_TRANSIT, MOTORCYCLE
}

enum class SessionStatus {
    ACTIVE, PAUSED, COMPLETED, CANCELLED
}
