package com.blank.commutetrack.feature.history

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
import com.blank.commutetrack.core.common.extension.formatTime
import com.blank.commutetrack.core.ui.component.SessionCard
import com.blank.commutetrack.core.ui.theme.CommuteColors

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
        } else if (uiState.sessions.isEmpty()) {
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
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = CommuteColors.SlateGreen
                    )
                    Text(
                        "No trips yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = CommuteColors.SlateGreen
                    )
                    Text(
                        "Start tracking your first commute",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CommuteColors.MutedGreen
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Filter chips
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPeriod.entries.forEach { period ->
                            FilterChip(
                                selected = uiState.filterPeriod == period,
                                onClick = { viewModel.setFilterPeriod(period) },
                                label = {
                                    Text(period.name.lowercase().replaceFirstChar { it.uppercase() })
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

                item {
                    Text(
                        "${uiState.sessions.size} Trips",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                items(uiState.sessions) { session ->
                    SessionCard(
                        startLocation = session.startLocation,
                        endLocation = session.endLocation.ifEmpty { "In progress" },
                        transportMode = session.transportMode.name,
                        duration = session.durationMinutes.formatDuration(),
                        distance = session.distanceKm.formatDistance(),
                        time = session.startTime.formatTime(),
                        status = session.status.name
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
