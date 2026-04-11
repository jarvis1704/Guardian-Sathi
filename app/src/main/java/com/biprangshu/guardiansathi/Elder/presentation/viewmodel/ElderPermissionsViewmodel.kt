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


@HiltViewModel
class ElderPermissionsViewmodel @Inject constructor(
    private val permissionManager: PermissionManagerRepository
): ViewModel() {
    private val _permissionstate = MutableStateFlow(ElderPermissionsState())
    val permissionstate = _permissionstate.asStateFlow()


    fun checkPermissions(){
        if (_permissionstate.value.isUpdating){
            return
        }
        _permissionstate.update {
            it.copy(isUpdating = true)
        }

        //check each permission
//        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            arrayOf(
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.POST_NOTIFICATIONS
//            )
//        } else {
//            arrayOf(
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            _permissionstate.update {
//                it.copy(notificationPermissionGranted = true)
//            }
//        }

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

    }
}