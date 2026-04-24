package com.biprangshu.guardiansathi.Global.Elder.presentation.viewmodel

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.Elder.data.PermissionManagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject


data class ElderPermissionsState(
    val isUpdating: Boolean = false,
    val locationPermissionGranted: Boolean = false,
    val backgroundLocationPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val activityRecognitionGranted: Boolean = false,
    val smsReadGranted: Boolean = false,
    val phonePermissionsGranted: Boolean = false,
    val phoneLogPermissionGranted: Boolean = false,
)

data class ElderPermissionAlert(
    val showLocationAlert: Boolean = false,
    val showBackgroundLocationAlert: Boolean = false,
    val showNotificationAlert: Boolean = false,
    val showActivityRecognitionAlert: Boolean = false,
    val showReadSmsAlert: Boolean = false,
    val showPhoneAlert: Boolean = false,
    val showPhoneLogAlert: Boolean = false,
)

data class SpecialElderPermissionState(
    val isBatteryOptimizationIgnored: Boolean = false,
    val isNotificationListenerEnabled: Boolean = false,
    val isFullScreenIntentGranted: Boolean = false
)

data class SpecialPermissionAlertState(
    val showBatteryOptimizationAlert: Boolean = false,
    val showNotificationListenerAlert: Boolean = false,
    val showFullScreenIntentAlert: Boolean = false
)

@HiltViewModel
class ElderPermissionsViewmodel @Inject constructor(
    private val permissionManager: PermissionManagerRepository
): ViewModel() {
    private val _permissionstate = MutableStateFlow(ElderPermissionsState())
    val permissionstate = _permissionstate.asStateFlow()

    private val _permissionAlertState = MutableStateFlow(ElderPermissionAlert())
    val permissionAlertState = _permissionAlertState.asStateFlow()

    private val _specialPermissionState = MutableStateFlow(SpecialElderPermissionState())
    val specialPermissionState = _specialPermissionState.asStateFlow()

    private val _specialPermissionAlertState = MutableStateFlow(SpecialPermissionAlertState())
    val specialPermissionAlertState = _specialPermissionAlertState.asStateFlow()


    val allPermissionsGranted: StateFlow<Boolean> = combine(
        _permissionstate,
        _specialPermissionState
    ) { permState, specialState ->
        permState.locationPermissionGranted
                && permState.notificationPermissionGranted
                && permState.backgroundLocationPermissionGranted
                && permState.activityRecognitionGranted
                && permState.smsReadGranted
                && permState.phonePermissionsGranted
                && permState.phoneLogPermissionGranted
                && specialState.isBatteryOptimizationIgnored
                && specialState.isNotificationListenerEnabled
                && specialState.isFullScreenIntentGranted

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )


    init {
        checkPermissions()
    }

    fun checkPermissions() {
        if (_permissionstate.value.isUpdating) {
            return
        }
        _permissionstate.update {
            it.copy(isUpdating = true)
        }

        val locationGranted = permissionManager.isPermissionGranted(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionManager.isPermissionGranted(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            true
        }
        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionManager.isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
        val activityGranted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            true
        } else {
            permissionManager.isPermissionGranted(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        val smsReadGranted = permissionManager.isPermissionGranted(
            Manifest.permission.READ_SMS
        )
        val phonePermissionsGranted = permissionManager.isPermissionGranted(
            Manifest.permission.READ_PHONE_STATE
        )
        val phoneLogPermissionGranted = permissionManager.isPermissionGranted(
            Manifest.permission.READ_CALL_LOG
        )

        _permissionstate.update {
            it.copy(
                isUpdating = false,
                locationPermissionGranted = locationGranted,
                backgroundLocationPermissionGranted = backgroundLocationGranted,
                notificationPermissionGranted = notificationGranted,
                activityRecognitionGranted = activityGranted,
                smsReadGranted = smsReadGranted,
                phonePermissionsGranted = phonePermissionsGranted,
                phoneLogPermissionGranted = phoneLogPermissionGranted
            )
        }

        _permissionAlertState.update {
            it.copy(
                showLocationAlert = !locationGranted,
                showBackgroundLocationAlert = !backgroundLocationGranted,
                showNotificationAlert = !notificationGranted,
                showActivityRecognitionAlert = !activityGranted,
                showReadSmsAlert = !smsReadGranted,
                showPhoneAlert = !phonePermissionsGranted,
                showPhoneLogAlert = !phoneLogPermissionGranted
            )
        }

        checkSpecialPermissions()
    }

    fun checkSpecialPermissions() {
        val batteryOp = permissionManager.isBatteryOptimizationIgnored()
        val notifListener = permissionManager.isNotificationListenerEnabled()
        val fullScreenIntent = permissionManager.isFullScreenIntentEnabled()

        _specialPermissionState.update {
            Log.d("SpecialPermCheck", "Updating specialPermissionState")
            it.copy(
                isBatteryOptimizationIgnored = batteryOp,
                isNotificationListenerEnabled = notifListener,
                isFullScreenIntentGranted = fullScreenIntent
            )
        }

        _specialPermissionAlertState.update {
            it.copy(
                showBatteryOptimizationAlert = !batteryOp,
                showNotificationListenerAlert = !notifListener,
                showFullScreenIntentAlert = !fullScreenIntent
            )
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _permissionstate.update { it.copy(locationPermissionGranted = granted) }
        _permissionAlertState.update { it.copy(showLocationAlert = false) }
    }

    fun onBackgroundLocationPermissionResult(granted: Boolean) {
        _permissionstate.update { it.copy(backgroundLocationPermissionGranted = granted) }
        _permissionAlertState.update { it.copy(showBackgroundLocationAlert = false) }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        _permissionstate.update { it.copy(notificationPermissionGranted = granted) }
        _permissionAlertState.update { it.copy(showNotificationAlert = false) }
    }

    fun onActivityRecognitionPermissionResult(granted: Boolean) {
        _permissionstate.update { it.copy(activityRecognitionGranted = granted) }
        _permissionAlertState.update { it.copy(showActivityRecognitionAlert = false) }
    }

//    fun onRecordAudioPermissionResult(granted: Boolean) {
//        _permissionstate.update { it.copy(recordAudioGranted = granted) }
//        _permissionAlertState.update { it.copy(showRecordAudioAlert = false) }
//        checkPermissions()
//    }
//
//    fun onCameraPermissionResult(granted: Boolean) {
//        _permissionstate.update { it.copy(cameraGranted = granted) }
//        _permissionAlertState.update { it.copy(showCameraAlert = false) }
//        checkPermissions()
//    }

    fun onSmsReadPermissionResult(granted: Boolean) {
        _permissionstate.update { it.copy(smsReadGranted = granted) }
        _permissionAlertState.update { it.copy(showReadSmsAlert = false) }
    }

    fun onPhonePermissionResult(granted: Boolean) {
        _permissionstate.update { it.copy(phonePermissionsGranted = granted) }
        _permissionAlertState.update { it.copy(showPhoneAlert = false) }
    }

    fun onPhoneLogPermissionResult(granted: Boolean) {
        _permissionstate.update { it.copy(phoneLogPermissionGranted = granted) }
        _permissionAlertState.update { it.copy(showPhoneLogAlert = false) }
    }
}