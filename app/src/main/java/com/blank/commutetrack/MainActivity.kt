package com.blank.commutetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.blank.commutetrack.core.ui.theme.CommuteColors
import com.blank.commutetrack.core.ui.theme.CommuteTrackTheme
import com.blank.commutetrack.feature.dashboard.DashboardScreen
import com.blank.commutetrack.feature.history.HistoryScreen
import com.blank.commutetrack.feature.settings.SettingsScreen
import com.blank.commutetrack.feature.statistics.StatisticsScreen
import com.blank.commutetrack.feature.tracking.TrackingScreen
import com.blank.commutetrack.ui.navigation.CommuteNavGraph
import com.blank.commutetrack.ui.navigation.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CommuteTrackApp()
        }
    }
}

@Composable
fun CommuteTrackApp() {
    CommuteTrackTheme {
        val navController = rememberNavController()
        val viewModel: MainViewModel = hiltViewModel()
        val bottomBarState by viewModel.bottomBarState.collectAsStateWithLifecycle()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val showBottomBar = currentDestination?.route in bottomBarState.visibleRoutes

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0.dp),
            bottomBar = {
                if (showBottomBar) {
                    com.blank.commutetrack.core.ui.component.CommuteBottomBar(
                        currentRoute = currentDestination?.route ?: "dashboard",
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        ) { padding ->
            CommuteNavGraph(
                navController = navController,
                modifier = Modifier.padding(padding),
                onBottomBarVisibleChange = { visible ->
                    viewModel.updateBottomBarVisibility(visible)
                }
            )
        }
    }
}
