package com.blank.commutetrack.core.domain.model

data class UserSettings(
    val defaultTransportMode: TransportMode = TransportMode.DRIVING,
    val distanceUnit: DistanceUnit = DistanceUnit.KILOMETERS,
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val autoStartEnabled: Boolean = false,
    val homeAddress: String = "",
    val workAddress: String = ""
)

enum class DistanceUnit {
    KILOMETERS, MILES
}
