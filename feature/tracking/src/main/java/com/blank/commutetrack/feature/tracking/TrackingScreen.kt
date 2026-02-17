package com.blank.commutetrack.feature.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.blank.commutetrack.core.ui.component.TransportModeChip
import com.blank.commutetrack.core.ui.theme.ActiveGreen

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Active trip card or start trip form
                if (uiState.isTracking) {
                    // Active Trip Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = ActiveGreen.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = ActiveGreen.copy(alpha = 0.2f),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.DirectionsRun,
                                            contentDescription = null,
                                            tint = ActiveGreen,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        "Trip in Progress",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        uiState.elapsedTime,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = ActiveGreen
                                    )
                                }
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "From",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        uiState.activeSession?.startLocation ?: "",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Column {
                                    Text(
                                        "To",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Tap to set",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Button(
                                onClick = { showEndTripDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("End Trip")
                            }
                        }
                    }
                } else {
                    // Start Trip Form
                    OutlinedTextField(
                        value = uiState.startLocation,
                        onValueChange = { viewModel.updateStartLocation(it) },
                        label = { Text("Start Location") },
                        placeholder = { Text("Enter starting point") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        "Transport Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(com.blank.commutetrack.core.domain.model.TransportMode.entries.size) { index ->
                            val mode = com.blank.commutetrack.core.domain.model.TransportMode.entries[index]
                            TransportModeChip(
                                mode = mode.name,
                                isSelected = uiState.selectedTransportMode == mode,
                                onClick = { viewModel.selectTransportMode(mode) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { viewModel.startTrip() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = uiState.startLocation.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActiveGreen
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Start Trip",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
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
fun EndTripDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var endLocation by remember { mutableStateOf("") }
    var distanceKm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("End Trip") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = endLocation,
                    onValueChange = { endLocation = it },
                    label = { Text("End Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = distanceKm,
                    onValueChange = { distanceKm = it },
                    label = { Text("Distance (km)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val distance = distanceKm.toDoubleOrNull() ?: 0.0
                    onConfirm(endLocation, distance)
                },
                enabled = endLocation.isNotBlank() && distanceKm.isNotBlank()
            ) {
                Text("End Trip")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
