package com.blank.commutetrack.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.blank.commutetrack.core.domain.model.TransportMode
import com.blank.commutetrack.core.ui.component.TransportModeChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Preferences Section
                Text(
                    "Preferences",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Default Transport Mode
                        Column {
                            Text(
                                "Default Transport",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
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

                        HorizontalDivider()

                        // Distance Unit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Distance Unit",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Choose your preferred unit",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row {
                                FilterChip(
                                    selected = uiState.settings.distanceUnit == com.blank.commutetrack.core.domain.model.DistanceUnit.KILOMETERS,
                                    onClick = { viewModel.updateDistanceUnit(com.blank.commutetrack.core.domain.model.DistanceUnit.KILOMETERS) },
                                    label = { Text("km") }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FilterChip(
                                    selected = uiState.settings.distanceUnit == com.blank.commutetrack.core.domain.model.DistanceUnit.MILES,
                                    onClick = { viewModel.updateDistanceUnit(com.blank.commutetrack.core.domain.model.DistanceUnit.MILES) },
                                    label = { Text("mi") }
                                )
                            }
                        }
                    }
                }

                // Notifications Section
                Text(
                    "Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
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

                // Appearance Section
                Text(
                    "Appearance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingToggle(
                            title = "Dark Mode",
                            description = "Use dark theme",
                            checked = uiState.settings.darkModeEnabled,
                            onCheckedChange = { viewModel.toggleDarkMode(it) }
                        )
                    }
                }

                // Locations Section
                Text(
                    "Saved Locations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
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
                                Icon(Icons.Default.Home, contentDescription = null)
                            }
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
                                Icon(Icons.Default.Work, contentDescription = null)
                            }
                        )
                    }
                }

                // Auto-start Section
                Text(
                    "Automation",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingToggle(
                            title = "Auto-start Tracking",
                            description = "Automatically start tracking when leaving saved locations",
                            checked = uiState.settings.autoStartEnabled,
                            onCheckedChange = { viewModel.toggleAutoStart(it) }
                        )
                    }
                }

                // About Section
                Text(
                    "About",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "CommuteTrack",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Version 1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Your daily commute companion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
