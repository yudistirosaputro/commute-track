package com.blank.commutetrack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.blank.commutetrack.feature.dashboard.DashboardScreen
import com.blank.commutetrack.feature.history.HistoryScreen
import com.blank.commutetrack.feature.settings.SettingsScreen
import com.blank.commutetrack.feature.statistics.StatisticsScreen
import com.blank.commutetrack.feature.tracking.TrackingScreen

@Composable
fun CommuteNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onBottomBarVisibleChange: (Boolean) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = modifier
    ) {
        composable("dashboard") {
            onBottomBarVisibleChange(true)
            DashboardScreen(
                onNavigateToTracking = {
                    navController.navigate("tracking")
                },
                onNavigateToHistory = {
                    navController.navigate("history")
                }
            )
        }

        composable("statistics") {
            onBottomBarVisibleChange(true)
            StatisticsScreen()
        }

        composable("tracking") {
            onBottomBarVisibleChange(false)
            TrackingScreen()
        }

        composable("history") {
            onBottomBarVisibleChange(true)
            HistoryScreen()
        }

        composable("settings") {
            onBottomBarVisibleChange(true)
            SettingsScreen()
        }
    }
}
