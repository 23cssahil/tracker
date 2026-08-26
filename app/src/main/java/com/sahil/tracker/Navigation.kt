package com.sahil.tracker

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sahil.tracker.ui.appstats.AppStatsScreen
import com.sahil.tracker.ui.dashboard.DashboardScreen
import com.sahil.tracker.ui.notes.NotesScreen
import com.sahil.tracker.ui.settings.SettingsScreen
import com.sahil.tracker.ui.timelog.TimeLogScreen

data class NavItem(val route: String, val icon: ImageVector, val label: String)

val navItems = listOf(
    NavItem("dashboard", Icons.Default.Home, "Dashboard"),
    NavItem("appstats", Icons.Outlined.BarChart, "Apps"),
    NavItem("timelog", Icons.Default.List, "Time Log"),
    NavItem("notes", Icons.Outlined.Edit, "Notes"),
    NavItem("settings", Icons.Default.Settings, "Settings"),
)

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {
            composable("dashboard") { DashboardScreen() }
            composable("appstats") { AppStatsScreen() }
            composable("timelog") { TimeLogScreen() }
            composable("notes") { NotesScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
