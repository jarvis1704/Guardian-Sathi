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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.ManageHistory
import androidx.compose.material.icons.rounded.MarkChatUnread
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.guardiansathi.Elder.core.GuardianService
import com.biprangshu.guardiansathi.Elder.core.getDetailedBatteryInfo
import com.biprangshu.guardiansathi.Elder.core.getLocationFlow
import com.biprangshu.guardiansathi.Elder.presentation.Components.PermissionAlertDialog
import com.biprangshu.guardiansathi.Elder.presentation.viewmodel.ElderPermissionsViewmodel
import kotlinx.coroutines.launch

@Composable
fun ElderHomeScreen(
    elderPermissionsViewmodel: ElderPermissionsViewmodel = hiltViewModel()
) {
    //basic permission launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        elderPermissionsViewmodel.onLocationPermissionResult(granted)
    }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        elderPermissionsViewmodel.onBackgroundLocationPermissionResult(granted)
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        elderPermissionsViewmodel.onNotificationPermissionResult(granted)
    }

    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        elderPermissionsViewmodel.onActivityRecognitionPermissionResult(granted)
    }

    val smsReadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        elderPermissionsViewmodel.onSmsReadPermissionResult(granted)
    }

    val phonePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        elderPermissionsViewmodel.onPhonePermissionResult(granted)
    }

    val phoneLogPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        elderPermissionsViewmodel.onPhoneLogPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        elderPermissionsViewmodel.checkPermissions()
    }
    val context = LocalContext.current

    val permissionState by elderPermissionsViewmodel.permissionstate.collectAsStateWithLifecycle()
    val permissionAlertState by elderPermissionsViewmodel.permissionAlertState.collectAsStateWithLifecycle()

    if (permissionAlertState.showLocationAlert) {
        PermissionAlertDialog(
            title = "Location Access",
            subtitle = "Needed for your safety",
            reason1 = "Your guardian can see your location in real time",
            reason2 = "Required for emergency SOS and geo-fence alerts",
            disclaimer = "Your location is only shared with your trusted guardian. We never share it with anyone else.",
            buttonText = "Allow location access",
            icon = Icons.Rounded.PinDrop,
            onContinue = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        )
    }

    if (permissionAlertState.showBackgroundLocationAlert) {
        PermissionAlertDialog(
            title = "Always-On Location",
            subtitle = "Protection even when app is closed",
            reason1 = "Geo-fence alerts work even when GuardianSathi is in the background",
            reason2 = "Your guardian is notified if you leave a safe area at any time",
            disclaimer = "Background location is only used for safety monitoring and is never sold or shared.",
            buttonText = "Allow background location",
            icon = Icons.Rounded.TrackChanges,
            onContinue = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLocationLauncher.launch(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                }
            }
        )
    }

    if (permissionAlertState.showNotificationAlert) {
        PermissionAlertDialog(
            title = "Stay Notified",
            subtitle = "Important alerts need your attention",
            reason1 = "Receive medicine reminders and health check-in alerts on time",
            reason2 = "Your guardian can send you urgent messages instantly",
            disclaimer = "We only send notifications that matter to your safety and health.",
            buttonText = "Allow notifications",
            icon = Icons.Rounded.NotificationsActive,
            onContinue = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        )
    }

    if (permissionAlertState.showActivityRecognitionAlert) {
        PermissionAlertDialog(
            title = "Fall Detection",
            subtitle = "We watch over you silently",
            reason1 = "Detects sudden falls and immediately alerts your guardian",
            reason2 = "Monitors movement patterns to identify unusual stillness after a fall",
            disclaimer = "Motion data is processed on your device and never uploaded to any server.",
            buttonText = "Enable fall detection",
            icon = Icons.Rounded.Accessibility,
            onContinue = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    activityRecognitionLauncher.launch(
                        Manifest.permission.ACTIVITY_RECOGNITION
                    )
                }
            }
        )
    }

    if (permissionAlertState.showReadSmsAlert) {
        PermissionAlertDialog(
            title = "SMS Protection",
            subtitle = "Guard against scam messages",
            reason1 = "Scans incoming messages for known scam and phishing patterns",
            reason2 = "Alerts your guardian before you accidentally respond to a fraud",
            disclaimer = "Your messages are scanned locally on your device. We never read or store your personal SMS.",
            buttonText = "Enable SMS protection",
            icon = Icons.Rounded.MarkChatUnread,
            onContinue = {
                smsReadLauncher.launch(
                    Manifest.permission.READ_SMS
                )
            }
        )
    }

    if (permissionAlertState.showPhoneAlert) {
        PermissionAlertDialog(
            title = "Call Protection",
            subtitle = "Detect scam calls in real time",
            reason1 = "Identifies incoming calls from known scam and fraud numbers",
            reason2 = "Your guardian is alerted when a suspicious call is received",
            disclaimer = "Call data is only used for scam detection and is never stored or shared.",
            buttonText = "Enable call protection",
            icon = Icons.Rounded.PhoneInTalk,
            onContinue = {
                phonePermissionsLauncher.launch(
                    Manifest.permission.READ_PHONE_STATE
                )
            }
        )
    }

    if (permissionAlertState.showPhoneLogAlert) {
        PermissionAlertDialog(
            title = "Call History Access",
            subtitle = "Identify suspicious call patterns",
            reason1 = "Analyzes call history to detect repeated contact from fraud numbers",
            reason2 = "Helps your guardian understand unusual calling patterns over time",
            disclaimer = "Call history is analyzed locally and only flagged entries are shared with your guardian.",
            buttonText = "Allow call history access",
            icon = Icons.Rounded.ManageHistory,
            onContinue = {
                phoneLogPermissionsLauncher.launch(
                    Manifest.permission.READ_CALL_LOG
                )
            }
        )
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
                false -> {
                    Text("Elder Home - Guardian Service Active! ✓")
                }
                false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Background location permission needed")
                        Text("This allows us to protect you even when the app is closed")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
//                            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
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
//                            basicPermissionLauncher.launch(permissionsToRequest)
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