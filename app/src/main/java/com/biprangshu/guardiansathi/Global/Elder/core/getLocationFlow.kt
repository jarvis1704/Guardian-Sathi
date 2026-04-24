package com.biprangshu.guardiansathi.Global.Elder.core

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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

@SuppressLint("MissingPermission")
suspend fun getLastKnownLocation(context: Context): Pair<Double, Double>? {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    return suspendCancellableCoroutine { continuation ->
        fusedClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("Location", "Got location: ${location.latitude}, ${location.longitude}")
                    continuation.resume(Pair(location.latitude, location.longitude))
                } else {
                    Log.d("Location", "Last location is null — requesting fresh")
                    continuation.resume(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Location", "Failed: ${e.message}")
                continuation.resume(null)
            }
    }
}