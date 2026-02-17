package com.blank.commutetrack.core.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.blank.commutetrack.core.ui.theme.*

@Composable
fun TransportModeChip(
    mode: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, color) = getTransportModeIconAndColor(mode)

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(mode.lowercase().replaceFirstChar { it.uppercase() }) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = mode,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color,
            selectedLeadingIconColor = color
        ),
        modifier = modifier
    )
}

fun getTransportModeIconAndColor(mode: String): Pair<ImageVector, Color> = when (mode.uppercase()) {
    "WALKING" -> Icons.Default.DirectionsWalk to WalkingColor
    "CYCLING" -> Icons.Default.DirectionsBike to CyclingColor
    "DRIVING" -> Icons.Default.DirectionsCar to DrivingColor
    "PUBLIC_TRANSIT" -> Icons.Default.DirectionsBus to TransitColor
    "MOTORCYCLE" -> Icons.Default.TwoWheeler to MotorcycleColor
    else -> Icons.Default.DirectionsCar to DrivingColor
}
