package com.blank.commutetrack.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.blank.commutetrack.core.ui.theme.CommuteColors

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("dashboard", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("statistics", "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem("tracking", "Track", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle),
    BottomNavItem("history", "History", Icons.Filled.History, Icons.Outlined.History),
    BottomNavItem("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun CommuteBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = CommuteColors.DarkestGreen,
        contentColor = CommuteColors.NeonGreen
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CommuteColors.NeonGreen,
                    selectedTextColor = CommuteColors.NeonGreen,
                    unselectedIconColor = CommuteColors.SlateGreen,
                    unselectedTextColor = CommuteColors.SlateGreen,
                    indicatorColor = CommuteColors.NeonGreen.copy(alpha = 0.12f)
                )
            )
        }
    }
}
