package com.biprangshu.guardiansathi.Elder.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.ManageHistory
import androidx.compose.material.icons.rounded.MarkChatUnread
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.guardiansathi.Elder.core.GuardianService
import com.biprangshu.guardiansathi.Elder.core.getDetailedBatteryInfo
import com.biprangshu.guardiansathi.Elder.core.getLocationFlow
import com.biprangshu.guardiansathi.Elder.presentation.Components.PermissionAlertDialog
import com.biprangshu.guardiansathi.Elder.presentation.viewmodel.ElderPermissionsViewmodel
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.biprangshu.guardiansathi.R

@Composable
fun ElderHomeScreen(
    elderPermissionsViewmodel: ElderPermissionsViewmodel = hiltViewModel()
) {
    // re-check special permissions every time user returns to screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                elderPermissionsViewmodel.checkSpecialPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
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

//    LaunchedEffect(Unit) {
//        elderPermissionsViewmodel.checkPermissions()
//    }
    val context = LocalContext.current

    val permissionState by elderPermissionsViewmodel.permissionstate.collectAsStateWithLifecycle()
    val permissionAlertState by elderPermissionsViewmodel.permissionAlertState.collectAsStateWithLifecycle()
    val specialPermissionAlertState by elderPermissionsViewmodel.specialPermissionAlertState.collectAsStateWithLifecycle()

    //all permission alert dialogues
    if (specialPermissionAlertState.showBatteryOptimizationAlert) {
        PermissionAlertDialog(
            title = stringResource(R.string.ElderPermission_1_T),
            subtitle = stringResource(R.string.ElderPermission_1_S),
            reason1 = stringResource(R.string.ElderPermission_1_R1),
            reason2 = stringResource(R.string.ElderPermission_1_R2),
            disclaimer = stringResource(R.string.ElderPermission_1_D),
            icon = Icons.Rounded.BatteryChargingFull,
            onContinue = {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = "package:${context.packageName}".toUri()
                }
                context.startActivity(intent)
            }
        )
    }

    if (specialPermissionAlertState.showNotificationListenerAlert) {
        PermissionAlertDialog(
            title = stringResource(R.string.ElderPermission_2_T),
            subtitle = stringResource(R.string.ElderPermission_2_S),
            reason1 = stringResource(R.string.ElderPermission_2_R1),
            reason2 = stringResource(R.string.ElderPermission_2_R2),
            disclaimer = stringResource(R.string.ElderPermission_2_D),
            icon = Icons.Rounded.NotificationsNone,
            onContinue = {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                context.startActivity(intent)
            }
        )
    }

    if (permissionState.locationPermissionGranted &&
        permissionAlertState.showBackgroundLocationAlert &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        PermissionAlertDialog(
            title = stringResource(R.string.ElderPermission_4_T),
            subtitle = stringResource(R.string.ElderPermission_4_S),
            reason1 = stringResource(R.string.ElderPermission_4_R1),
            reason2 = stringResource(R.string.ElderPermission_4_R2),
            disclaimer = stringResource(R.string.ElderPermission_4_D),
            icon = Icons.Rounded.TrackChanges,
            onContinue = {
                backgroundLocationLauncher.launch(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        )
    }

    if (permissionAlertState.showLocationAlert) {
        PermissionAlertDialog(
            title = stringResource(R.string.ElderPermission_3_T),
            subtitle = stringResource(R.string.ElderPermission_3_S),
            reason1 = stringResource(R.string.ElderPermission_3_R1),
            reason2 = stringResource(R.string.ElderPermission_3_R2),
            disclaimer = stringResource(R.string.ElderPermission_3_D),
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

    if (permissionAlertState.showNotificationAlert) {
        PermissionAlertDialog(
            title = stringResource(R.string.ElderPermission_5_T),
            subtitle = stringResource(R.string.ElderPermission_5_S),
            reason1 = stringResource(R.string.ElderPermission_5_R1),
            reason2 = stringResource(R.string.ElderPermission_5_R2),
            disclaimer = stringResource(R.string.ElderPermission_5_D),
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
            title = stringResource(R.string.ElderPermission_6_T),
            subtitle = stringResource(R.string.ElderPermission_6_S),
            reason1 = stringResource(R.string.ElderPermission_6_R1),
            reason2 = stringResource(R.string.ElderPermission_6_R2),
            disclaimer = stringResource(R.string.ElderPermission_6_D),
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
            title = stringResource(R.string.ElderPermission_7_T),
            subtitle = stringResource(R.string.ElderPermission_7_S),
            reason1 = stringResource(R.string.ElderPermission_7_R1),
            reason2 = stringResource(R.string.ElderPermission_7_R2),
            disclaimer = stringResource(R.string.ElderPermission_7_D),
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
            title = stringResource(R.string.ElderPermission_8_T),
            subtitle = stringResource(R.string.ElderPermission_8_S),
            reason1 = stringResource(R.string.ElderPermission_8_R1),
            reason2 = stringResource(R.string.ElderPermission_8_R2),
            disclaimer = stringResource(R.string.ElderPermission_8_D),
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
            title = stringResource(R.string.ElderPermission_9_T),
            subtitle = stringResource(R.string.ElderPermission_9_S),
            reason1 = stringResource(R.string.ElderPermission_9_R1),
            reason2 = stringResource(R.string.ElderPermission_9_R2),
            disclaimer = stringResource(R.string.ElderPermission_9_D),
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