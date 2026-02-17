package com.blank.commutetrack.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blank.commutetrack.core.common.extension.formatDistance
import com.blank.commutetrack.core.common.extension.formatDuration
import com.blank.commutetrack.core.common.extension.formatTime
import com.blank.commutetrack.core.ui.component.SessionCard
import com.blank.commutetrack.core.ui.component.StatCard
import com.blank.commutetrack.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTracking: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "CommuteTrack",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Your daily commute companion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                // Active Session Banner
                uiState.activeSession?.let { session ->
                    item {
                        Card(
                            onClick = onNavigateToTracking,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = ActiveGreen.copy(alpha = 0.2f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.NearMe,
                                            contentDescription = null,
                                            tint = ActiveGreen
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Active Trip",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "From ${session.startLocation}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                }

                // Today's Stats
                item {
                    Text(
                        "Today's Summary",
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
                            title = "Trips",
                            value = "${uiState.todayStats.tripsCount}",
                            icon = Icons.Default.SwapCalls,
                            iconTint = CompletedBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Distance",
                            value = uiState.todayStats.totalDistanceKm.formatDistance(),
                            icon = Icons.Default.Route,
                            iconTint = ActiveGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Time",
                            value = uiState.todayStats.totalDurationMinutes.formatDuration(),
                            icon = Icons.Default.Schedule,
                            iconTint = PausedAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Quick Start Button
                item {
                    Button(
                        onClick = onNavigateToTracking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Start New Trip",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Weekly Overview
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "This Week",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Total Trips",
                            value = "${uiState.weeklyStats.totalTrips}",
                            subtitle = "this week",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Avg Duration",
                            value = uiState.weeklyStats.averageDurationMinutes.formatDuration(),
                            subtitle = "per trip",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Recent Trips
                if (uiState.recentTrips.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Recent Trips",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = onNavigateToHistory) {
                                Text("See all")
                            }
                        }
                    }

                    items(uiState.recentTrips) { session ->
                        SessionCard(
                            startLocation = session.startLocation,
                            endLocation = session.endLocation,
                            transportMode = session.transportMode.name,
                            duration = session.durationMinutes.formatDuration(),
                            distance = session.distanceKm.formatDistance(),
                            time = session.startTime.formatTime(),
                            status = session.status.name
                        )
                    }
                }
            }
        }
    }
}
