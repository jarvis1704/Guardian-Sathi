package com.biprangshu.guardiansathi.Elder.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Elder.core.getLastKnownLocation
import com.biprangshu.guardiansathi.Elder.data.GoogleLocationRepository
import com.biprangshu.guardiansathi.Elder.presentation.screens.EmergencyNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmergencyNumbersState(
    val isFetching: Boolean = false,
    val emergencyNumbers: List<EmergencyNumber> = emptyList()
)

@HiltViewModel
class EmergencyNumbersViewmodel @Inject constructor(
    private val repository: GoogleLocationRepository
): ViewModel() {
    private val _emergencyNumbersState = MutableStateFlow(EmergencyNumbersState())
    val emergencyNumbersState = _emergencyNumbersState.asStateFlow()

    fun loadEmergencyNumbers(context: Context) {
        if (_emergencyNumbersState.value.isFetching) return

        viewModelScope.launch {
            Log.d("places_api","trying to get nearby places")
            _emergencyNumbersState.update { it.copy(isFetching = true) }
            try {
                val location = getLastKnownLocation(context)
                val latitude = location?.first ?:0.0
                val longitude = location?.second ?:0.0
                Log.d("places_api","got location: $latitude, $longitude")
                val numbers = repository.getNearbyEmergencyNumbers(latitude, longitude)
                _emergencyNumbersState.update {
                    it.copy(isFetching = false, emergencyNumbers = numbers)
                }
            } catch (e: Exception) {
                _emergencyNumbersState.update { it.copy(isFetching = false) }
                Log.e("EmergencyVM", "Failed: ${e.message}")
            }
        }
    }
}