package com.biprangshu.guardiansathi.Global.Guardian.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.core.data.FirebaseAuthDataSource
import com.biprangshu.guardiansathi.Global.core.data.FirestoreLinkDataSource
import com.biprangshu.guardiansathi.Global.core.domain.Result
import com.biprangshu.guardiansathi.Global.presentation.ui.components.errorMessage
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GuardianGeofenceState(
    val isLoading: Boolean = true,
    val centerLat: Double? = null,
    val centerLong: Double? = null,
    val radiusMeters: Double = 500.0,
    val isActive: Boolean = false,
    val elderLocationLat: Double? = null,
    val elderLocationLong: Double? = null
)

sealed interface GuardianGeofenceAction {
    data class OnMapClick(val latLng: LatLng) : GuardianGeofenceAction
    data class OnRadiusChange(val radius: Float) : GuardianGeofenceAction
    data class OnToggleActive(val active: Boolean) : GuardianGeofenceAction
    data object OnSaveClick : GuardianGeofenceAction
}

sealed interface GuardianGeofenceEvent {
    data class ShowSnackbar(val message: String) : GuardianGeofenceEvent
}

@HiltViewModel
class GuardianGeofenceViewModel @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firebaseDatabase: FirebaseDatabase,
    private val firestoreLinkDataSource: FirestoreLinkDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(GuardianGeofenceState())
    val state = _state.asStateFlow()

    private val _events = Channel<GuardianGeofenceEvent>()
    val events = _events.receiveAsFlow()

    private var elderUid: String? = null

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val guardianUid = firebaseAuthDataSource.getCurentUserUid()
            if (guardianUid == null) {
                _events.send(GuardianGeofenceEvent.ShowSnackbar("User not logged in"))
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            val linkResult = firestoreLinkDataSource.getLinkStatus(guardianUid)
            elderUid = when (linkResult) {
                is Result.Success -> linkResult.data.linkedUid
                else -> null
            }

            if (elderUid == null) {
                _events.send(GuardianGeofenceEvent.ShowSnackbar("No Elder linked"))
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            val elderRef = firebaseDatabase.reference.child(elderUid!!)
            
            // Listen to Elder location
            elderRef.child("location_lat").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lat = snapshot.getValue(String::class.java)?.toDoubleOrNull()
                    _state.update { it.copy(elderLocationLat = lat) }
                    // Default center to elder location if not set
                    if (_state.value.centerLat == null && lat != null) {
                        _state.update { it.copy(centerLat = lat) }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    errorMessage = error.message.toString()
                }
            })
            
            elderRef.child("location_long").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lng = snapshot.getValue(String::class.java)?.toDoubleOrNull()
                    _state.update { it.copy(elderLocationLong = lng) }
                    if (_state.value.centerLong == null && lng != null) {
                        _state.update { it.copy(centerLong = lng) }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    errorMessage = error.message.toString()
                }
            })

            // Load geofence data
            elderRef.child("geofence").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lat = snapshot.child("center_lat").getValue(Double::class.java)
                    val lng = snapshot.child("center_long").getValue(Double::class.java)
                    val rad = snapshot.child("radius_meters").getValue(Double::class.java)
                    val active = snapshot.child("is_active").getValue(Boolean::class.java) ?: false
                    
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            centerLat = lat ?: it.centerLat,
                            centerLong = lng ?: it.centerLong,
                            radiusMeters = rad ?: it.radiusMeters,
                            isActive = active
                        )
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    _state.update { it.copy(isLoading = false) }
                }
            })
        }
    }

    fun onAction(action: GuardianGeofenceAction) {
        when (action) {
            is GuardianGeofenceAction.OnMapClick -> {
                _state.update { 
                    it.copy(centerLat = action.latLng.latitude, centerLong = action.latLng.longitude)
                }
            }
            is GuardianGeofenceAction.OnRadiusChange -> {
                _state.update { it.copy(radiusMeters = action.radius.toDouble()) }
            }
            is GuardianGeofenceAction.OnToggleActive -> {
                _state.update { it.copy(isActive = action.active) }
            }
            GuardianGeofenceAction.OnSaveClick -> {
                saveGeofence()
            }
        }
    }

    private fun saveGeofence() {
        val currentUid = elderUid
        if (currentUid == null) {
            viewModelScope.launch { _events.send(GuardianGeofenceEvent.ShowSnackbar("Elder not linked")) }
            return
        }

        val currentState = _state.value
        if (currentState.centerLat == null || currentState.centerLong == null) {
            viewModelScope.launch { _events.send(GuardianGeofenceEvent.ShowSnackbar("Please tap the map to select a center point")) }
            return
        }

        _state.update { it.copy(isLoading = true) }
        
        val geofenceData = mapOf(
            "center_lat" to currentState.centerLat,
            "center_long" to currentState.centerLong,
            "radius_meters" to currentState.radiusMeters,
            "is_active" to currentState.isActive
        )

        firebaseDatabase.reference
            .child(currentUid)
            .child("geofence")
            .setValue(geofenceData)
            .addOnSuccessListener {
                _state.update { it.copy(isLoading = false) }
                viewModelScope.launch { _events.send(GuardianGeofenceEvent.ShowSnackbar("Geofence saved successfully")) }
            }
            .addOnFailureListener {
                _state.update { it.copy(isLoading = false) }
                viewModelScope.launch { _events.send(GuardianGeofenceEvent.ShowSnackbar("Failed to save Geofence")) }
            }
    }
}
