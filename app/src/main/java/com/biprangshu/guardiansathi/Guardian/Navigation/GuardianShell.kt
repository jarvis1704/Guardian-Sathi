package com.biprangshu.guardiansathi.Guardian.Navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.biprangshu.guardiansathi.Guardian.presentation.screens.GuardianAlertPage
import com.biprangshu.guardiansathi.Guardian.presentation.screens.GuardianHomeRoot
import com.biprangshu.guardiansathi.Guardian.presentation.screens.GuardianProfileRoot
import com.biprangshu.guardiansathi.Guardian.presentation.screens.GuardianGeofenceRoot

// guardian/navigation/GuardianShell.kt

@Composable
fun GuardianShell(outerNavController: NavController, onLogout: () -> Unit = {}) {
    val guardianNavController = rememberNavController()
    val navBackStackEntry by guardianNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            BottomBar(
                currentDestination =  currentDestination,
                guardianNavController = guardianNavController
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = guardianNavController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<DashboardRoute> {
                GuardianHomeRoot()
            }
            composable<LocationRoute> {
                GuardianGeofenceRoot()
            }
            composable<AlertsRoute> {
                GuardianAlertPage()
            }
            composable<SettingsRoute> {
                GuardianProfileRoot(onLogout = onLogout)
            }
        }
    }
}


@Composable
fun BottomBar(
    currentDestination:  NavDestination?,
    guardianNavController: NavController
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        guardianTabs.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any {
                it.hasRoute(tab.route::class)
            } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    guardianNavController.navigate(tab.route) {
                        // Pop up to the start destination — avoids
                        // a huge backstack when switching tabs
                        popUpTo(guardianNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) }
            )
        }
    }
}