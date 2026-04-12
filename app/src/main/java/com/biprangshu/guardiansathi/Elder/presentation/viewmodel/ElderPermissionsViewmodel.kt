package com.biprangshu.guardiansathi.Elder.presentation.viewmodel

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import com.biprangshu.guardiansathi.Elder.data.PermissionManagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


data class ElderPermissionsState(
    val isUpdating: Boolean = false,
    val locationPermissionGranted: Boolean = false,
    val backgroundLocationPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
)

data class ElderPermissionAlert(
    val showLocationAlert: Boolean = false,
    val showBackgroundLocationAlert: Boolean = false,
    val showNotificationAlert: Boolean = false
)

@HiltViewModel
class ElderPermissionsViewmodel @Inject constructor(
    private val permissionManager: PermissionManagerRepository
): ViewModel() {
    private val _permissionstate = MutableStateFlow(ElderPermissionsState())
    val permissionstate = _permissionstate.asStateFlow()

    private val _permissionAlertState = MutableStateFlow(ElderPermissionAlert())
    val permissionAlertState = _permissionAlertState.asStateFlow()


    fun checkPermissions(){
        if (_permissionstate.value.isUpdating){
            return
        }
        _permissionstate.update {
            it.copy(isUpdating = true)
        }

        val locationGranted = permissionManager.isPermissionGranted(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val backgroundLocationGranted = permissionManager.isPermissionGranted(
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            permissionManager.isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
        _permissionstate.update {
            it.copy(
                isUpdating = false,
                locationPermissionGranted = locationGranted,
                backgroundLocationPermissionGranted = backgroundLocationGranted,
                notificationPermissionGranted = notificationGranted
            )
        }
        AskRequiredPermissions()
    }

    fun AskRequiredPermissions(){
        if (!_permissionstate.value.locationPermissionGranted){
            _permissionAlertState.update {
                it.copy(
                    showLocationAlert = true
                )
            }
        }
        if (!_permissionstate.value.backgroundLocationPermissionGranted){
            _permissionAlertState.update {
                it.copy(
                    showBackgroundLocationAlert = true
                )
            }
        }
        if (!_permissionstate.value.notificationPermissionGranted){
            _permissionAlertState.update {
                it.copy(
                    showNotificationAlert = true
                )
            }
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
}