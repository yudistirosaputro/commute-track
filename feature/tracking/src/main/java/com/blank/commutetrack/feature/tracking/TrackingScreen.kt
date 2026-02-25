package com.blank.commutetrack.feature.tracking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
    val context = LocalContext.current
    
    // Permission handling
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }
    
    // Initialize ViewModel and fetch current location
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
        viewModel.fetchCurrentLocation(context)
    }
    
    // Bind to service when screen is visible
    DisposableEffect(Unit) {
        viewModel.bindService(context)
        onDispose {
            viewModel.unbindService(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                title = {
                    Text(
                        if (uiState.isTracking) "Active Trip" else "Start Trip",
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
        bottomBar = {
            if (uiState.isTracking) {
                // Circle action buttons at bottom
                TrackingBottomActions(
                    isPaused = uiState.isPaused,
                    onPause = { viewModel.pauseTrip() },
                    onResume = { viewModel.resumeTrip() },
                    onStop = { showEndTripDialog = true }
                )
            }
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
                // Permission Banner
                if (!hasLocationPermission) {
                    item {
                        PermissionBanner(
                            title = "Location Permission Required",
                            message = "Location access is needed to track your trip and calculate distance automatically.",
                            onGrantClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        )
                    }
                }
                
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    item {
                        PermissionBanner(
                            title = "Notification Permission",
                            message = "Notifications are needed to show ongoing trip status.",
                            onGrantClick = {
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            }
                        )
                    }
                }

                if (uiState.isTracking) {
                    // Active Trip Content - Scrollable above the circle button
                    item {
                        ActiveTripContent(uiState = uiState)
                    }
                } else {
                    // Start Trip Form
                    item {
                        StartTripForm(
                            uiState = uiState,
                            hasLocationPermission = hasLocationPermission,
                            onLocationChange = { viewModel.updateStartLocation(it) },
                            onTransportSelect = { viewModel.selectTransportMode(it) },
                            onStartTrip = {
                                if (!hasLocationPermission) {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                } else {
                                    viewModel.startTrip(context)
                                }
                            }
                        )
                    }
                }
            }

            if (showEndTripDialog) {
                EndTripDialog(
                    uiState = uiState,
                    viewModel = viewModel,
                    context = context,
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
private fun PermissionBanner(
    title: String,
    message: String,
    onGrantClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = CommuteColors.PausedAmber.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, CommuteColors.PausedAmber.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = CommuteColors.PausedAmber
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CommuteColors.PausedAmber
                )
            }
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = CommuteColors.SlateGreen
            )
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CommuteColors.PausedAmber,
                    contentColor = CommuteColors.DarkestGreen
                )
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun ActiveTripContent(uiState: TrackingUiState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = CommuteColors.GlassyCard
        ),
        border = BorderStroke(1.dp, CommuteColors.BorderGreen),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Large monospace timer - centered
            Text(
                text = uiState.elapsedTime,
                fontFamily = FontFamily.Monospace,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = if (uiState.isPaused) CommuteColors.PausedAmber else CommuteColors.NeonGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = CommuteColors.BorderGreen)

            // Trip info - 3 columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TripInfoColumn(
                    label = "Started",
                    value = uiState.activeSession?.startTime?.let {
                        "%02d:%02d".format(it.hour, it.minute)
                    } ?: "--:--"
                )
                TripInfoColumn(
                    label = "From",
                    value = uiState.activeSession?.startLocation ?: "--"
                )
                TripInfoColumn(
                    label = "Pauses",
                    value = "${uiState.pauseCount}",
                    valueColor = if (uiState.pauseCount > 0) CommuteColors.PausedAmber else Color.White
                )
            }

            HorizontalDivider(color = CommuteColors.BorderGreen)

            // Distance (auto-calculated)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Route,
                    contentDescription = null,
                    tint = CommuteColors.NeonGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = uiState.formattedDistance,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = CommuteColors.NeonGreen
                )
            }
            Text(
                text = "Auto-calculated from GPS",
                style = MaterialTheme.typography.bodySmall,
                color = CommuteColors.SlateGreen
            )

            HorizontalDivider(color = CommuteColors.BorderGreen)

            // Transport mode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (uiState.activeSession?.transportMode?.name) {
                        "WALKING" -> Icons.Default.DirectionsWalk
                        "CYCLING" -> Icons.Default.PedalBike
                        "DRIVING" -> Icons.Default.DirectionsCar
                        "PUBLIC_TRANSIT" -> Icons.Default.DirectionsBus
                        "MOTORCYCLE" -> Icons.Default.TwoWheeler
                        else -> Icons.Default.Commute
                    },
                    contentDescription = null,
                    tint = CommuteColors.SlateGreen
                )
                Text(
                    text = uiState.activeSession?.transportMode?.name?.lowercase()
                        ?.replaceFirstChar { it.uppercase() } ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TripInfoColumn(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CommuteColors.SlateGreen
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun TrackingBottomActions(
    isPaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        color = CommuteColors.DarkestGreen,
        tonalElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause/Resume Circle Button
                if (isPaused) {
                    CircleActionButton(
                        onClick = onResume,
                        icon = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        containerColor = CommuteColors.NeonGreen,
                        contentColor = CommuteColors.DarkestGreen
                    )
                } else {
                    CircleActionButton(
                        onClick = onPause,
                        icon = Icons.Default.Pause,
                        contentDescription = "Pause",
                        containerColor = CommuteColors.PausedAmber,
                        contentColor = CommuteColors.DarkestGreen
                    )
                }

                // Stop Circle Button
                CircleActionButton(
                    onClick = onStop,
                    icon = Icons.Default.Stop,
                    contentDescription = "Stop",
                    containerColor = CommuteColors.ErrorRed,
                    contentColor = Color.White
                )
            }
        }
    }
}

@Composable
private fun CircleActionButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    size: Int = 72
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        shadowElevation = 8.dp,
        modifier = Modifier.size(size.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size((size / 2).dp)
            )
        }
    }
}

@Composable
private fun StartTripForm(
    uiState: TrackingUiState,
    hasLocationPermission: Boolean,
    onLocationChange: (String) -> Unit,
    onTransportSelect: (com.blank.commutetrack.core.domain.model.TransportMode) -> Unit,
    onStartTrip: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Auto-detected location info
        if (hasLocationPermission && uiState.autoDetectedLocation.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CommuteColors.NeonGreen.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, CommuteColors.NeonGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = CommuteColors.NeonGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Detected Location",
                            style = MaterialTheme.typography.labelSmall,
                            color = CommuteColors.SlateGreen
                        )
                        Text(
                            uiState.autoDetectedLocation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        OutlinedTextField(
            value = uiState.startLocation,
            onValueChange = onLocationChange,
            label = { Text("Start Location") },
            placeholder = { Text("Enter starting point or use GPS") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = CommuteColors.NeonGreen)
            },
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

        // Horizontally scrollable transport mode chips
        androidx.compose.foundation.lazy.LazyRow(
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

        Spacer(modifier = Modifier.height(32.dp))

        // Large circle start button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val isEnabled = uiState.startLocation.isNotBlank()
            Surface(
                shape = CircleShape,
                color = if (isEnabled) 
                    CommuteColors.NeonGreen 
                else 
                    CommuteColors.BorderGreen,
                shadowElevation = if (isEnabled) 12.dp else 0.dp,
                modifier = Modifier.size(120.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = isEnabled, onClick = onStartTrip)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        tint = CommuteColors.DarkestGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "START",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = CommuteColors.DarkestGreen
                    )
                }
            }
        }
    }
}

@Composable
fun EndTripDialog(
    uiState: TrackingUiState,
    viewModel: TrackingViewModel,
    context: Context,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var endLocation by remember { mutableStateOf("") }
    var distanceKm by remember { mutableStateOf("") }
    var isLoadingAddress by remember { mutableStateOf(true) }
    
    // Pre-fill with calculated distance
    LaunchedEffect(uiState.calculatedDistanceKm) {
        if (uiState.calculatedDistanceKm > 0 && distanceKm.isBlank()) {
            distanceKm = "%.2f".format(uiState.calculatedDistanceKm)
        }
    }
    
    // Fetch end location automatically
    LaunchedEffect(Unit) {
        isLoadingAddress = true
        val address = viewModel.getCurrentEndAddress(context)
        if (address.isNotBlank() && endLocation.isBlank()) {
            endLocation = address
        }
        isLoadingAddress = false
    }

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
                // Show auto-calculated distance info
                if (uiState.calculatedDistanceKm > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CommuteColors.NeonGreen.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, CommuteColors.NeonGreen.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Route,
                                contentDescription = null,
                                tint = CommuteColors.NeonGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "GPS Calculated Distance",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CommuteColors.SlateGreen
                                )
                                Text(
                                    uiState.formattedDistance,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CommuteColors.NeonGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = endLocation,
                    onValueChange = { endLocation = it },
                    label = { Text("End Location") },
                    placeholder = { Text("Fetching current location...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingAddress,
                    leadingIcon = {
                        if (isLoadingAddress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = CommuteColors.NeonGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = CommuteColors.NeonGreen
                            )
                        }
                    },
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
