package com.blank.commutetrack.feature.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blank.commutetrack.core.common.extension.formatDistance
import com.blank.commutetrack.core.common.extension.formatDuration
import com.blank.commutetrack.core.common.extension.formatTime
import com.blank.commutetrack.core.ui.theme.CommuteColors
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                title = {
                    Text(
                        "Trip History",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CommuteColors.DarkestGreen,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = CommuteColors.DarkestGreen
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CommuteColors.NeonGreen)
            }
        } else if (uiState.filteredSessions.isEmpty()) {
            EmptyHistoryView(padding = padding)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Card
                item {
                    HistorySummaryCard(sessions = uiState.filteredSessions)
                }

                // Filter chips - horizontally scrollable
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(FilterPeriod.entries.size) { index ->
                            val period = FilterPeriod.entries[index]
                            FilterChip(
                                selected = uiState.filterPeriod == period,
                                onClick = { viewModel.setFilterPeriod(period) },
                                label = {
                                    Text(
                                        when (period) {
                                            FilterPeriod.ALL -> "All Time"
                                            FilterPeriod.WEEK -> "This Week"
                                            FilterPeriod.MONTH -> "This Month"
                                            FilterPeriod.YEAR -> "This Year"
                                        }
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CommuteColors.NeonGreen.copy(alpha = 0.15f),
                                    selectedLabelColor = CommuteColors.NeonGreen,
                                    labelColor = CommuteColors.SlateGreen
                                )
                            )
                        }
                    }
                }

                // Section header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${uiState.filteredSessions.size} Trips",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Trip list
                items(
                    items = uiState.filteredSessions,
                    key = { it.id }
                ) { session ->
                    HistoryItemCard(session = session)
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun HistorySummaryCard(sessions: List<com.blank.commutetrack.core.domain.model.CommuteSession>) {
    val totalTrips = sessions.size
    val totalDistance = sessions.sumOf { it.distanceKm }
    val totalDuration = sessions.sumOf { it.durationMinutes }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CommuteColors.NeonGreen.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, CommuteColors.NeonGreen.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Total Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = CommuteColors.NeonGreen
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    value = "$totalTrips",
                    label = "Trips",
                    icon = Icons.Default.Route
                )
                SummaryStatItem(
                    value = totalDistance.formatDistance(),
                    label = "Distance",
                    icon = Icons.Default.Place
                )
                SummaryStatItem(
                    value = totalDuration.formatDuration(),
                    label = "Time",
                    icon = Icons.Default.Schedule
                )
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CommuteColors.NeonGreen,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = CommuteColors.SlateGreen
        )
    }
}

@Composable
private fun HistoryItemCard(
    session: com.blank.commutetrack.core.domain.model.CommuteSession
) {
    val (icon, color) = getTransportModeInfo(session.transportMode.name)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CommuteColors.GlassyCard
        ),
        border = BorderStroke(1.dp, CommuteColors.BorderGreen)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transport icon
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = session.transportMode.name,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Trip details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${session.startLocation} → ${session.endLocation.ifEmpty { "..." }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // Stats row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatWithIcon(
                        icon = Icons.Default.Schedule,
                        value = session.durationMinutes.formatDuration()
                    )
                    StatWithIcon(
                        icon = Icons.Default.Route,
                        value = session.distanceKm.formatDistance()
                    )
                }
            }

            // Time and status
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = session.startTime.formatTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = CommuteColors.SlateGreen
                )
                
                val statusColor = when (session.status.name) {
                    "ACTIVE" -> CommuteColors.NeonGreen
                    "PAUSED" -> CommuteColors.PausedAmber
                    "COMPLETED" -> Color(0xFF4A9EFF)
                    else -> CommuteColors.ErrorRed
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = session.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = CommuteColors.SlateGreen
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = CommuteColors.SlateGreen
        )
    }
}

@Composable
private fun EmptyHistoryView(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = CommuteColors.GlassyCard,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = CommuteColors.SlateGreen
                    )
                }
            }
            Text(
                "No trips yet",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Start tracking your first commute",
                style = MaterialTheme.typography.bodyMedium,
                color = CommuteColors.SlateGreen
            )
        }
    }
}

private fun getTransportModeInfo(mode: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    return when (mode) {
        "WALK" -> Icons.Default.DirectionsWalk to Color(0xFF66BB6A)
        "BIKE" -> Icons.Default.PedalBike to Color(0xFF42A5F5)
        "CAR" -> Icons.Default.DirectionsCar to Color(0xFFEF5350)
        "BUS" -> Icons.Default.DirectionsBus to Color(0xFFAB47BC)
        "TRAIN" -> Icons.Default.Train to Color(0xFFAB47BC)
        "SUBWAY" -> Icons.Default.Subway to Color(0xFFAB47BC)
        "MOTORCYCLE" -> Icons.Default.TwoWheeler to Color(0xFFFFA726)
        else -> Icons.Default.Commute to CommuteColors.SlateGreen
    }
}
