package com.blank.commutetrack.feature.tracking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blank.commutetrack.core.ui.component.TransportModeChip
import com.blank.commutetrack.core.ui.theme.CommuteColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEndTripDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Track Commute",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (uiState.isTracking) {
                    // Active Trip Card
                    ActiveTripCard(
                        uiState = uiState,
                        onPause = { viewModel.pauseTrip() },
                        onResume = { viewModel.resumeTrip() },
                        onStop = { showEndTripDialog = true }
                    )
                } else {
                    // Start Trip Form
                    StartTripForm(
                        uiState = uiState,
                        onLocationChange = { viewModel.updateStartLocation(it) },
                        onTransportSelect = { viewModel.selectTransportMode(it) },
                        onStartTrip = { viewModel.startTrip() }
                    )
                }
            }

            if (showEndTripDialog) {
                EndTripDialog(
                    onDismiss = { showEndTripDialog = false },
                    onConfirm = { endLocation, distance ->
                        viewModel.endTrip(endLocation, distance)
                        showEndTripDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ActiveTripCard(
    uiState: TrackingUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = CommuteColors.GlassyCard
        ),
        border = BorderStroke(1.dp, CommuteColors.BorderGreen),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Status chip
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (uiState.isPaused)
                    CommuteColors.PausedAmber.copy(alpha = 0.15f)
                else
                    CommuteColors.NeonGreen.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (uiState.isPaused) "PAUSED" else "ACTIVE",
                    color = if (uiState.isPaused) CommuteColors.PausedAmber else CommuteColors.NeonGreen,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Large monospace timer
            Text(
                text = uiState.elapsedTime,
                fontFamily = FontFamily.Monospace,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = if (uiState.isPaused) CommuteColors.PausedAmber else CommuteColors.NeonGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Trip info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Started",
                        style = MaterialTheme.typography.labelSmall,
                        color = CommuteColors.SlateGreen
                    )
                    Text(
                        uiState.activeSession?.startTime?.let {
                            "%02d:%02d".format(it.hour, it.minute)
                        } ?: "--:--",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        "From",
                        style = MaterialTheme.typography.labelSmall,
                        color = CommuteColors.SlateGreen
                    )
                    Text(
                        uiState.activeSession?.startLocation ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Pauses",
                        style = MaterialTheme.typography.labelSmall,
                        color = CommuteColors.SlateGreen
                    )
                    Text(
                        "${uiState.pauseCount}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (uiState.pauseCount > 0) CommuteColors.PausedAmber else Color.White
                    )
                }
            }

            HorizontalDivider(color = CommuteColors.BorderGreen)

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.isPaused) {
                    Button(
                        onClick = onResume,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CommuteColors.NeonGreen,
                            contentColor = CommuteColors.DarkestGreen
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("RESUME", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onPause,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CommuteColors.PausedAmber,
                            contentColor = CommuteColors.DarkestGreen
                        )
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("PAUSE", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    border = BorderStroke(2.dp, CommuteColors.ErrorRed),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CommuteColors.ErrorRed
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("STOP", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StartTripForm(
    uiState: TrackingUiState,
    onLocationChange: (String) -> Unit,
    onTransportSelect: (com.blank.commutetrack.core.domain.model.TransportMode) -> Unit,
    onStartTrip: () -> Unit
) {
    OutlinedTextField(
        value = uiState.startLocation,
        onValueChange = onLocationChange,
        label = { Text("Start Location") },
        placeholder = { Text("Enter starting point") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CommuteColors.NeonGreen,
            unfocusedBorderColor = CommuteColors.BorderGreen,
            focusedLabelColor = CommuteColors.NeonGreen,
            cursorColor = CommuteColors.NeonGreen,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )

    Text(
        "Transport Mode",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.White
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(com.blank.commutetrack.core.domain.model.TransportMode.entries.size) { index ->
            val mode = com.blank.commutetrack.core.domain.model.TransportMode.entries[index]
            TransportModeChip(
                mode = mode.name,
                isSelected = uiState.selectedTransportMode == mode,
                onClick = { onTransportSelect(mode) }
            )
        }
    }

    Spacer(modifier = Modifier.fillMaxWidth().height(32.dp))

    Button(
        onClick = onStartTrip,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        enabled = uiState.startLocation.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
            containerColor = CommuteColors.NeonGreen,
            contentColor = CommuteColors.DarkestGreen,
            disabledContainerColor = CommuteColors.BorderGreen,
            disabledContentColor = CommuteColors.SlateGreen
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Icon(
            Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Start Trip",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EndTripDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var endLocation by remember { mutableStateOf("") }
    var distanceKm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CommuteColors.GlassyCard,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { Text("End Trip", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = endLocation,
                    onValueChange = { endLocation = it },
                    label = { Text("End Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CommuteColors.NeonGreen,
                        unfocusedBorderColor = CommuteColors.BorderGreen,
                        focusedLabelColor = CommuteColors.NeonGreen,
                        cursorColor = CommuteColors.NeonGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = distanceKm,
                    onValueChange = { distanceKm = it },
                    label = { Text("Distance (km)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CommuteColors.NeonGreen,
                        unfocusedBorderColor = CommuteColors.BorderGreen,
                        focusedLabelColor = CommuteColors.NeonGreen,
                        cursorColor = CommuteColors.NeonGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val distance = distanceKm.toDoubleOrNull() ?: 0.0
                    onConfirm(endLocation, distance)
                },
                enabled = endLocation.isNotBlank() && distanceKm.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CommuteColors.NeonGreen,
                    contentColor = CommuteColors.DarkestGreen
                )
            ) {
                Text("End Trip", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = CommuteColors.SlateGreen
                )
            ) {
                Text("Cancel")
            }
        }
    )
}
