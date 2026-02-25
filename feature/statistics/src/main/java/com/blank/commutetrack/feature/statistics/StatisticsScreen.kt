package com.blank.commutetrack.feature.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blank.commutetrack.core.common.extension.formatDistance
import com.blank.commutetrack.core.common.extension.formatDuration
import com.blank.commutetrack.core.domain.model.DepartureTimeStats
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
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                title = {
                    Text(
                        "Statistics",
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
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CommuteColors.NeonGreen.copy(alpha = 0.15f),
                                    selectedLabelColor = CommuteColors.NeonGreen
                                )
                            )
                        }
                    }
                }

                // Optimal Departure Time Card
                item {
                    OptimalDepartureCard(
                        bestTime = uiState.bestDepartureTime,
                        worstTime = uiState.worstDepartureTime
                    )
                }

                // Overview Cards
                item {
                    Text(
                        "Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
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

                // Departure Time Chart
                if (uiState.departureTimeAnalysis.isNotEmpty()) {
                    item {
                        Text(
                            "Duration by Departure Time",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    item {
                        DepartureTimeChart(
                            data = uiState.departureTimeAnalysis,
                            bestHour = uiState.bestDepartureTime?.hourOfDay
                        )
                    }
                }

                // Total Time Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CommuteColors.GlassyCard
                        ),
                        border = BorderStroke(1.dp, CommuteColors.NeonGreen.copy(alpha = 0.3f))
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
                                tint = CommuteColors.NeonGreen
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Total Commute Time",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CommuteColors.SlateGreen
                                )
                                Text(
                                    uiState.statistics.totalDurationMinutes.formatDuration(),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = CommuteColors.NeonGreen
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
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    items(uiState.transportBreakdown.toList()) { (mode, count) ->
                        val (icon, color) = getTransportModeIconAndColor(mode.name)
                        val percentage = if (uiState.statistics.totalTrips > 0) {
                            (count.toFloat() / uiState.statistics.totalTrips * 100).toInt()
                        } else 0

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = CommuteColors.GlassyCard
                            ),
                            border = BorderStroke(1.dp, CommuteColors.BorderGreen)
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
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
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
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        "$percentage%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CommuteColors.SlateGreen
                                    )
                                }
                            }
                        }
                    }
                }

                // Weekly Activity Chart
                if (uiState.statistics.weeklyTrips.isNotEmpty()) {
                    item {
                        Text(
                            "Weekly Activity",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = CommuteColors.GlassyCard
                            ),
                            border = BorderStroke(1.dp, CommuteColors.BorderGreen)
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
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CommuteColors.NeonGreen
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            modifier = Modifier
                                                .width(32.dp)
                                                .height(((daily.tripCount.toFloat() / maxTrips) * 100).coerceAtLeast(8f).dp),
                                            color = CommuteColors.NeonGreen,
                                            shape = MaterialTheme.shapes.small
                                        ) {}
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            daily.dayOfWeek,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CommuteColors.SlateGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun OptimalDepartureCard(
    bestTime: DepartureTimeStats?,
    worstTime: DepartureTimeStats?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CommuteColors.GlassyCard),
        border = BorderStroke(1.dp, CommuteColors.NeonGreen)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Optimal Departure Time",
                style = MaterialTheme.typography.titleLarge,
                color = CommuteColors.NeonGreen,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            bestTime?.let {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "BEST TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = CommuteColors.SlateGreen
                        )
                        Text(
                            "%d:00".format(it.hourOfDay),
                            style = MaterialTheme.typography.headlineMedium,
                            color = CommuteColors.NeonGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Avg: ${it.averageDurationMinutes}m",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CommuteColors.SlateGreen
                        )
                    }

                    worstTime?.let { worst ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "WORST TIME",
                                style = MaterialTheme.typography.labelSmall,
                                color = CommuteColors.SlateGreen
                            )
                            Text(
                                "%d:00".format(worst.hourOfDay),
                                style = MaterialTheme.typography.headlineMedium,
                                color = CommuteColors.ErrorRed,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Avg: ${worst.averageDurationMinutes}m",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CommuteColors.SlateGreen
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                val timeSaved = worstTime?.averageDurationMinutes?.minus(it.averageDurationMinutes) ?: 0
                if (timeSaved > 0) {
                    Text(
                        "Leaving at ${it.hourOfDay}:00 saves ~${timeSaved} minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CommuteColors.SlateGreen
                    )
                }
            } ?: Text(
                "Need at least 10 trips to calculate optimal time",
                style = MaterialTheme.typography.bodyMedium,
                color = CommuteColors.SlateGreen
            )
        }
    }
}

@Composable
fun DepartureTimeChart(
    data: List<DepartureTimeStats>,
    bestHour: Int? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CommuteColors.GlassyCard),
        border = BorderStroke(1.dp, CommuteColors.BorderGreen)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val maxDuration = data.maxOfOrNull { it.averageDurationMinutes } ?: 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { stat ->
                    val isBest = stat.hourOfDay == bestHour
                    val barColor = if (isBest) CommuteColors.NeonGreen else CommuteColors.NeonGreen.copy(alpha = 0.4f)
                    val heightFraction = stat.averageDurationMinutes.toFloat() / maxDuration

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "${stat.averageDurationMinutes}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isBest) CommuteColors.NeonGreen else CommuteColors.SlateGreen,
                            fontSize = 9.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            modifier = Modifier
                                .width(24.dp)
                                .height((heightFraction * 120).coerceAtLeast(4f).dp),
                            color = barColor,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {}
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${stat.hourOfDay}h",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isBest) CommuteColors.NeonGreen else CommuteColors.SlateGreen,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    color = CommuteColors.NeonGreen,
                    shape = MaterialTheme.shapes.extraSmall
                ) {}
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Best departure time",
                    style = MaterialTheme.typography.labelSmall,
                    color = CommuteColors.SlateGreen
                )
            }
        }
    }
}
