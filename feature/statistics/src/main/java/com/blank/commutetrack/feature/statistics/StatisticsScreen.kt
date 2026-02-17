package com.blank.commutetrack.feature.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.blank.commutetrack.core.ui.component.StatCard
import com.blank.commutetrack.core.ui.component.getTransportModeIconAndColor
import com.blank.commutetrack.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Statistics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period Selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatsPeriod.entries.forEach { period ->
                            FilterChip(
                                selected = uiState.selectedPeriod == period,
                                onClick = { viewModel.selectPeriod(period) },
                                label = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Overview Cards
                item {
                    Text(
                        "Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Total Trips",
                            value = "${uiState.statistics.totalTrips}",
                            icon = Icons.Default.SwapCalls,
                            iconTint = CompletedBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Total Distance",
                            value = uiState.statistics.totalDistanceKm.formatDistance(),
                            icon = Icons.Default.Route,
                            iconTint = ActiveGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Avg Duration",
                            value = uiState.statistics.averageDurationMinutes.formatDuration(),
                            icon = Icons.Default.Timer,
                            iconTint = PausedAmber,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Avg Distance",
                            value = uiState.statistics.averageDistanceKm.formatDistance(),
                            icon = Icons.Default.Straighten,
                            iconTint = TransitColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Total Time
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Total Commute Time",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    uiState.statistics.totalDurationMinutes.formatDuration(),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Transport Breakdown
                if (uiState.transportBreakdown.isNotEmpty()) {
                    item {
                        Text(
                            "Transport Breakdown",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    items(uiState.transportBreakdown.toList()) { (mode, count) ->
                        val (icon, color) = getTransportModeIconAndColor(mode.name)
                        val percentage = if (uiState.statistics.totalTrips > 0) {
                            (count.toFloat() / uiState.statistics.totalTrips * 100).toInt()
                        } else 0

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = color.copy(alpha = 0.1f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(icon, contentDescription = null, tint = color)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        mode.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " "),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { percentage / 100f },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = color,
                                        trackColor = color.copy(alpha = 0.1f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "$count trips",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "$percentage%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Weekly Chart placeholder
                if (uiState.statistics.weeklyTrips.isNotEmpty()) {
                    item {
                        Text(
                            "Weekly Activity",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val maxTrips = uiState.statistics.weeklyTrips.maxOfOrNull { it.tripCount } ?: 1
                                uiState.statistics.weeklyTrips.forEach { daily ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom
                                    ) {
                                        Text(
                                            "${daily.tripCount}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            modifier = Modifier
                                                .width(32.dp)
                                                .height(((daily.tripCount.toFloat() / maxTrips) * 100).coerceAtLeast(8f).dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = MaterialTheme.shapes.small
                                        ) {}
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            daily.dayOfWeek,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom spacing
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
