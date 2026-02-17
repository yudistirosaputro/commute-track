package com.blank.commutetrack.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.blank.commutetrack.core.domain.model.TransportMode
import com.blank.commutetrack.core.ui.component.TransportModeChip
import com.blank.commutetrack.core.ui.theme.CommuteColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                title = {
                    Text(
                        "Settings",
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Preferences Section
                Text(
                    "Preferences",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CommuteColors.GlassyCard),
                    border = BorderStroke(1.dp, CommuteColors.BorderGreen)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                "Default Transport",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TransportMode.entries.forEach { mode ->
                                    TransportModeChip(
                                        mode = mode.name,
                                        isSelected = uiState.settings.defaultTransportMode == mode,
                                        onClick = { viewModel.updateDefaultTransport(mode) }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = CommuteColors.BorderGreen)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Distance Unit",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    "Choose your preferred unit",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CommuteColors.SlateGreen
                                )
                            }
                            Row {
                                FilterChip(
                                    selected = uiState.settings.distanceUnit == com.blank.commutetrack.core.domain.model.DistanceUnit.KILOMETERS,
                                    onClick = { viewModel.updateDistanceUnit(com.blank.commutetrack.core.domain.model.DistanceUnit.KILOMETERS) },
                                    label = { Text("km") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CommuteColors.NeonGreen.copy(alpha = 0.15f),
                                        selectedLabelColor = CommuteColors.NeonGreen
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FilterChip(
                                    selected = uiState.settings.distanceUnit == com.blank.commutetrack.core.domain.model.DistanceUnit.MILES,
                                    onClick = { viewModel.updateDistanceUnit(com.blank.commutetrack.core.domain.model.DistanceUnit.MILES) },
                                    label = { Text("mi") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CommuteColors.NeonGreen.copy(alpha = 0.15f),
                                        selectedLabelColor = CommuteColors.NeonGreen
                                    )
                                )
                            }
                        }
                    }
                }

                // Notifications Section
                Text(
                    "Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CommuteColors.GlassyCard),
                    border = BorderStroke(1.dp, CommuteColors.BorderGreen)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingToggle(
                            title = "Enable Notifications",
                            description = "Get reminded about your commutes",
                            checked = uiState.settings.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) }
                        )
                    }
                }

                // Locations Section
                Text(
                    "Saved Locations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CommuteColors.GlassyCard),
                    border = BorderStroke(1.dp, CommuteColors.BorderGreen)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        var homeText by remember { mutableStateOf(uiState.settings.homeAddress) }
                        var workText by remember { mutableStateOf(uiState.settings.workAddress) }

                        OutlinedTextField(
                            value = homeText,
                            onValueChange = {
                                homeText = it
                                viewModel.updateHomeAddress(it)
                            },
                            label = { Text("Home Address") },
                            placeholder = { Text("Enter your home address") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Home, contentDescription = null, tint = CommuteColors.NeonGreen)
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
                            value = workText,
                            onValueChange = {
                                workText = it
                                viewModel.updateWorkAddress(it)
                            },
                            label = { Text("Work Address") },
                            placeholder = { Text("Enter your work address") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Work, contentDescription = null, tint = CommuteColors.NeonGreen)
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
                    }
                }

                // Data Export Section
                Text(
                    "Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CommuteColors.GlassyCard),
                    border = BorderStroke(1.dp, CommuteColors.BorderGreen)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.exportData() },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, CommuteColors.NeonGreen),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CommuteColors.NeonGreen
                            )
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export for AI Analysis (CSV)")
                        }

                        Text(
                            "Export all completed trips as CSV for analysis in Python, Excel, or AI tools",
                            style = MaterialTheme.typography.bodySmall,
                            color = CommuteColors.SlateGreen
                        )
                    }
                }

                // About Section
                Text(
                    "About",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CommuteColors.GlassyCard),
                    border = BorderStroke(1.dp, CommuteColors.BorderGreen)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "CommuteTrack",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CommuteColors.NeonGreen
                        )
                        Text(
                            "Version 1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = CommuteColors.SlateGreen
                        )
                        Text(
                            "Your daily commute companion - Track, analyze, and optimize your commute times",
                            style = MaterialTheme.typography.bodySmall,
                            color = CommuteColors.SlateGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = CommuteColors.SlateGreen
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CommuteColors.DarkestGreen,
                checkedTrackColor = CommuteColors.NeonGreen,
                uncheckedThumbColor = CommuteColors.SlateGreen,
                uncheckedTrackColor = CommuteColors.BorderGreen
            )
        )
    }
}
