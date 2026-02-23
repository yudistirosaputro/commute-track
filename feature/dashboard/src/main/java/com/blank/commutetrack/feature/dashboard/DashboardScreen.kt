package com.blank.commutetrack.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                title = {
                    Column {
                        Text(
                            "CommuteTrack",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = CommuteColors.NeonGreen
                        )
                        Text(
                            "Your daily commute companion",
                            style = MaterialTheme.typography.bodySmall,
                            color = CommuteColors.SlateGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CommuteColors.DarkestGreen
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
                        ActiveSessionBanner(
                            session = session,
                            onClick = onNavigateToTracking
                        )
                    }
                }

                // Today's Stats
                item {
                    Text(
                        "Today's Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
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

                // Large Circle Start Button
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircleStartButton(onClick = onNavigateToTracking)
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
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
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
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            TextButton(
                                onClick = onNavigateToHistory,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = CommuteColors.NeonGreen
                                )
                            ) {
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

@Composable
private fun ActiveSessionBanner(
    session: com.blank.commutetrack.core.domain.model.CommuteSession,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = CommuteColors.NeonGreen.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, CommuteColors.NeonGreen.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = CommuteColors.NeonGreen.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.NearMe,
                        contentDescription = null,
                        tint = CommuteColors.NeonGreen
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Active Trip",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CommuteColors.NeonGreen
                )
                Text(
                    "From ${session.startLocation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CommuteColors.SlateGreen
                )
            }
            Icon(
                Icons.Default.ChevronRight, 
                contentDescription = null, 
                tint = CommuteColors.NeonGreen
            )
        }
    }
}

@Composable
private fun CircleStartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = CommuteColors.NeonGreen,
        shadowElevation = 12.dp,
        modifier = modifier
            .size(140.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = CommuteColors.NeonGreen,
                spotColor = CommuteColors.NeonGreen
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = CommuteColors.DarkestGreen,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "START",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CommuteColors.DarkestGreen
            )
        }
    }
}
