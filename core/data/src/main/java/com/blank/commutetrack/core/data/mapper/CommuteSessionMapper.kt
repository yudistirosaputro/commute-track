package com.blank.commutetrack.core.data.mapper

import com.blank.commutetrack.core.database.entity.CommuteSessionEntity
import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.model.SessionStatus
import com.blank.commutetrack.core.domain.model.TransportMode
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

fun CommuteSessionEntity.toDomain(): CommuteSession = CommuteSession(
    id = id,
    startTime = LocalDateTime.parse(startTime),
    endTime = endTime?.let { LocalDateTime.parse(it) },
    startLocation = startLocation,
    endLocation = endLocation,
    transportMode = TransportMode.valueOf(transportMode),
    distanceKm = distanceKm,
    durationMinutes = durationMinutes,
    status = SessionStatus.valueOf(status),
    date = LocalDate.parse(date),
    notes = notes,
    pausedMinutes = pausedMinutes,
    pauseCount = pauseCount,
    averageSpeedKmh = averageSpeedKmh
)

fun CommuteSession.toEntity(): CommuteSessionEntity = CommuteSessionEntity(
    id = id,
    startTime = startTime.toString(),
    endTime = endTime?.toString(),
    startLocation = startLocation,
    endLocation = endLocation,
    transportMode = transportMode.name,
    distanceKm = distanceKm,
    durationMinutes = durationMinutes,
    status = status.name,
    date = date.toString(),
    notes = notes,
    pausedMinutes = pausedMinutes,
    pauseCount = pauseCount,
    averageSpeedKmh = averageSpeedKmh
)
