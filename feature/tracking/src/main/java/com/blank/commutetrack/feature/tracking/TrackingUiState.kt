package com.blank.commutetrack.feature.tracking

import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.model.TransportMode

data class TrackingUiState(
    val isLoading: Boolean = true,
    val activeSession: CommuteSession? = null,
    val startLocation: String = "",
    val selectedTransportMode: TransportMode = TransportMode.DRIVING,
    val isTracking: Boolean = false,
    val elapsedTime: String = "0m"
)
