package com.blank.commutetrack.feature.tracking

import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.model.TransportMode

/**
 * UI State for the Tracking Screen.
 * 
 * @property elapsedSeconds Total elapsed seconds for the timer (updated every second)
 * @property autoDetectedLocation Location automatically detected by GPS
 * @property calculatedDistanceKm Distance calculated from GPS tracking in kilometers
 */
data class TrackingUiState(
    val isLoading: Boolean = true,
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val activeSession: CommuteSession? = null,
    val elapsedSeconds: Long = 0L,
    val startLocation: String = "",
    val selectedTransportMode: TransportMode = TransportMode.PUBLIC_TRANSIT,
    val pauseCount: Int = 0,
    val pausedMinutes: Int = 0,
    val autoDetectedLocation: String = "",
    val calculatedDistanceKm: Double = 0.0
) {
    /**
     * Formatted elapsed time string (HH:MM:SS or MM:SS)
     */
    val elapsedTime: String
        get() = formatElapsedTime(elapsedSeconds)
    
    /**
     * Formatted distance string with 2 decimal places
     */
    val formattedDistance: String
        get() = "%.2f km".format(calculatedDistanceKm)
}

private fun formatElapsedTime(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
