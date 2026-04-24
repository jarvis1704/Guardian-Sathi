package com.biprangshu.guardiansathi.Global.Elder.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.ManageHistory
import androidx.compose.material.icons.rounded.MarkChatUnread
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.guardiansathi.Global.Elder.core.GuardianService
import com.biprangshu.guardiansathi.Global.Elder.presentation.Components.PermissionAlertDialog
import com.biprangshu.guardiansathi.Global.Elder.presentation.viewmodel.ElderPermissionsViewmodel
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.biprangshu.guardiansathi.Global.Elder.presentation.viewmodel.ElderForegroundServiceViewmodel
import com.biprangshu.guardiansathi.Global.Elder.presentation.viewmodel.ElderHomeScreenViewModel
import com.biprangshu.guardiansathi.Global.Elder.presentation.viewmodel.RoomDBViewmodel
import com.biprangshu.guardiansathi.Global.core.isGestureNav
import com.biprangshu.guardiansathi.R



@Composable
fun ElderHomeScreen(
    onNavigateToEmergencyContacts: () -> Unit,
    onNavigateToPanicSOS: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToVoiceAssistant: () -> Unit,
    elderPermissionsViewmodel: ElderPermissionsViewmodel = hiltViewModel(),
    foregroundServiceViewmodel: ElderForegroundServiceViewmodel = hiltViewModel(),
    roomDBViewmodel: RoomDBViewmodel = hiltViewModel(),
    elderHomeScreenViewModel: ElderHomeScreenViewModel = hiltViewModel()
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

    val guardianPhotoURL by elderHomeScreenViewModel.guardianPhotoUrl.collectAsStateWithLifecycle(initialValue = null)
    val guardianName by elderHomeScreenViewModel.guardianName.collectAsStateWithLifecycle(initialValue = null)


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
    if (specialPermissionAlertState.showFullScreenIntentAlert) {
        PermissionAlertDialog(
            title = stringResource(R.string.ElderPermission_10_T),
            subtitle = stringResource(R.string.ElderPermission_10_S),
            reason1 = stringResource(R.string.ElderPermission_10_R1),
            reason2 = stringResource(R.string.ElderPermission_10_R2),
            disclaimer = stringResource(R.string.ElderPermission_10_D),
            icon = Icons.Rounded.Fullscreen,
            onContinue = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                        "package:${context.packageName}".toUri()
                    )
                    context.startActivity(intent)
                }
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

    val allPermissionsGranted by elderPermissionsViewmodel
        .allPermissionsGranted
        .collectAsStateWithLifecycle()

    val isForegroundServiceRunning = foregroundServiceViewmodel.serviceState.collectAsStateWithLifecycle()
    LaunchedEffect(allPermissionsGranted) {
        if (allPermissionsGranted){
            Log.d("GuardianService", "trying to start service")
            elderHomeScreenViewModel.getFCMTokenAndSave()
            foregroundServiceViewmodel.startService()
        }
    }

    //UI part starts here:

    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .padding(top = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // shield
            Box(
                modifier = Modifier
                    .padding(top = 30.dp)
            ){
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(if (allPermissionsGranted) R.raw.lottie_shield_green else R.raw.lottie_shield_red)
                )
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    speed = 0.4f
                )
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .size(210.dp)
                        .align(Alignment.Center)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(0.8f))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = guardianPhotoURL,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.ic_profile_placeholder),
                            placeholder = painterResource(R.drawable.ic_profile_placeholder)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = guardianName ?: "",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (allPermissionsGranted) stringResource(R.string.ElderHome_2) else stringResource(R.string.ElderHome_1),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

//            // status pills
//            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
//                StatusPill(
//                    icon = Icons.Rounded.BatteryFull,
//                    label = "80%"
//                )
//                StatusPill(
//                    icon = Icons.Rounded.LocationOn,
//                    label = if (true) "Location on" else "Location off"
//                )
//            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.SpaceAround
            ) {
                Spacer(Modifier.weight(1f))
                // voice assistant button
                Button(
                    onClick = {
                        onNavigateToVoiceAssistant()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.ElderHome_3),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(24.dp))

                //Emergency contacts
                Button(
                    onClick = {
                        onNavigateToEmergencyContacts()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Emergency,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.ElderHome_4),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.weight(1f))
                // SOS button
                Button(
                    onClick = {
                        onNavigateToPanicSOS()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(78.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(
                        2.dp,
                        Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Call,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.ElderHome_5),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.weight(0.3f))

                //small settings button
                TextButton(
                    onClick = {
                        onNavigateToSettings()
                    }
                ) {
                    Text(
                        stringResource(R.string.ElderHome_6),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.height(if (isGestureNav) 30.dp else 80.dp))
            }
        }
    }
}

@Composable
private fun StatusPill(
    icon: ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun startGuardianService(context: Context) {
    val serviceIntent = Intent(context, GuardianService::class.java)
    context.startForegroundService(serviceIntent)
}