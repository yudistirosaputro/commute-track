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
        label = { 
            Text(
                text = mode.lowercase().replaceFirstChar { it.uppercase() },
                color = if (isSelected) color else CommuteColors.SlateGreen
            ) 
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = mode,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) color else CommuteColors.SlateGreen
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color,
            selectedLeadingIconColor = color,
            containerColor = CommuteColors.DarkSurface,
            labelColor = CommuteColors.SlateGreen
        ),
        border = if (!isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = CommuteColors.BorderGreen
            )
        } else null,
        modifier = modifier
    )
}

fun getTransportModeIconAndColor(mode: String): Pair<ImageVector, Color> = when (mode.uppercase()) {
    "WALK", "WALKING" -> Icons.Default.DirectionsWalk to WalkingColor
    "BIKE", "CYCLING" -> Icons.Default.PedalBike to CyclingColor
    "CAR", "DRIVING" -> Icons.Default.DirectionsCar to DrivingColor
    "BUS", "PUBLIC_TRANSIT", "TRANSIT" -> Icons.Default.DirectionsBus to TransitColor
    "TRAIN" -> Icons.Default.Train to TransitColor
    "SUBWAY" -> Icons.Default.Subway to TransitColor
    "MOTORCYCLE" -> Icons.Default.TwoWheeler to MotorcycleColor
    else -> Icons.Default.DirectionsCar to DrivingColor
}
