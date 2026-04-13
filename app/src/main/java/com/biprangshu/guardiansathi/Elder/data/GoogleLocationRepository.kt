package com.biprangshu.guardiansathi.Elder.data

import com.biprangshu.guardiansathi.Elder.presentation.screens.EmergencyNumber

interface GoogleLocationRepository {
    suspend fun getNearbyEmergencyNumbers(latitude:Double, longitude:Double): List<EmergencyNumber>
    suspend fun fetchNearbyPlacesOfType(type: String, latitude: Double, longitude: Double): List<EmergencyNumber>
}