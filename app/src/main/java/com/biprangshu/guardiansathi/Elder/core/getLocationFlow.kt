package com.biprangshu.guardiansathi.Elder.core

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@SuppressLint("MissingPermission")
fun getLocationFlow(context: Context): Flow<Location> = callbackFlow {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    val request = LocationRequest.Builder(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        5000L // interval in ms
    ).apply {
        setMinUpdateIntervalMillis(2000L)
        setWaitForAccurateLocation(false)
    }.build()

    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { trySend(it) }
        }
    }

    fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

    awaitClose { fusedClient.removeLocationUpdates(callback) }
}