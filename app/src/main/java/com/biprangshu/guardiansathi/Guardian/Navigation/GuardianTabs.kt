package com.biprangshu.guardiansathi.Guardian.Navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable


@kotlinx.serialization.Serializable
object DashboardRoute
@kotlinx.serialization.Serializable
object LocationRoute
@kotlinx.serialization.Serializable
object AlertsRoute
@Serializable
object SettingsRoute

data class GuardianTab(
    val route: Any,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val guardianTabs = listOf(
    GuardianTab(DashboardRoute, "Dashboard", Icons.Outlined.Home, Icons.Filled.Home),
    GuardianTab(LocationRoute, "Location",  Icons.Outlined.LocationOn, Icons.Filled.LocationOn),
    GuardianTab(AlertsRoute,   "Alerts",    Icons.Outlined.Notifications, Icons.Filled.Notifications),
    GuardianTab(SettingsRoute, "Settings",  Icons.Outlined.Settings, Icons.Filled.Settings),
)