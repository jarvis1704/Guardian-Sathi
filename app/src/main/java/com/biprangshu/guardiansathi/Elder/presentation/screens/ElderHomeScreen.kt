package com.biprangshu.guardiansathi.Elder.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.biprangshu.guardiansathi.Elder.core.GuardianService
import com.biprangshu.guardiansathi.Elder.core.getDetailedBatteryInfo
import com.biprangshu.guardiansathi.Elder.core.getLocationFlow
import com.biprangshu.guardiansathi.Elder.presentation.Components.PermissionAlertDialog
import kotlinx.coroutines.launch

@Composable
fun ElderHomeScreen() {
    val context = LocalContext.current
    var basicPermissionsGranted by remember { mutableStateOf(false) }
    var backgroundPermissionGranted by remember { mutableStateOf(false) }
    var serviceStarted by remember { mutableStateOf(false) }

    // Launcher for basic permissions (location + notification)
    val basicPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        basicPermissionsGranted = allGranted

        if (allGranted) {
            // If Android 10+ (Q), need to request background location separately
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Will request background location next
            } else {
                // For older versions, start service directly
                startGuardianService(context)
                serviceStarted = true
            }
        }
    }

    // Launcher for background location (Android 10+)
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        backgroundPermissionGranted = isGranted
        // Start service regardless (background location is optional but recommended)
        startGuardianService(context)
        serviceStarted = true
    }
    LaunchedEffect(Unit) {
        // Request basic permissions first

    }

    // Request background location after basic permissions are granted
    LaunchedEffect(basicPermissionsGranted) {
        if (basicPermissionsGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    //UI part starts here:
    var currentLocation by remember { mutableStateOf<android.location.Location?>(null) }
    LaunchedEffect(Unit) {
        getLocationFlow(context).collect { location ->
            currentLocation = location
        }
    }

    val batteryinfo = getDetailedBatteryInfo(context)


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(

        ) {
            when {
                serviceStarted -> {
                    Text("Elder Home - Guardian Service Active! ✓")
                }
                basicPermissionsGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Background location permission needed")
                        Text("This allows us to protect you even when the app is closed")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }) {
                            Text("Grant Background Location")
                        }
                    }
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Permissions required for your protection")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            } else {
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            }
                            basicPermissionLauncher.launch(permissionsToRequest)
                        }) {
                            Text("Grant Permissions")
                        }
                    }
                }
            }
            Text("Battery info: $batteryinfo")
            Text("Current location: $currentLocation")
        }
    }
}

private fun startGuardianService(context: Context) {
    val serviceIntent = Intent(context, GuardianService::class.java)
    context.startForegroundService(serviceIntent)
}