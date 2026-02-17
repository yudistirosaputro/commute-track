package com.blank.commutetrack.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commute_sessions")
data class CommuteSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: String,
    val endTime: String? = null,
    val startLocation: String,
    val endLocation: String = "",
    val transportMode: String,
    val distanceKm: Double = 0.0,
    val durationMinutes: Int = 0,
    val status: String = "ACTIVE",
    val date: String,
    val notes: String = "",
    val pausedMinutes: Int = 0,
    val pauseCount: Int = 0,
    val averageSpeedKmh: Double = 0.0
)
