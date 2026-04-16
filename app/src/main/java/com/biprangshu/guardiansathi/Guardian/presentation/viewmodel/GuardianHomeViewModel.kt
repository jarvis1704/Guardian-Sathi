package com.biprangshu.guardiansathi.Guardian.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.core.data.FirebaseAuthDataSource
import com.biprangshu.guardiansathi.Global.core.data.FirestoreLinkDataSource
import com.biprangshu.guardiansathi.Global.core.domain.Result
import com.biprangshu.guardiansathi.Global.domain.SessionRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GuardianHomeState(
    val elderName: String = "",
    val elderPhotoUrl: String? = null,
    val guardianPhotoUrl: String? = null,
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val lastBatterySeen: Long = 0L,
    val locationLat: Double = 0.0,
    val locationLong: Double = 0.0,
    val lastLocationSeen: Long = 0L,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val lastActiveTimestamp: Long
        get() = maxOf(lastBatterySeen, lastLocationSeen)
}

sealed interface GuardianHomeAction {
    data object OnConfirmReminder : GuardianHomeAction
    data object OnSeeAllHistory : GuardianHomeAction
}

@HiltViewModel
class GuardianHomeViewModel @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firebaseDatabase: FirebaseDatabase,
    private val firestoreLinkDataSource: FirestoreLinkDataSource,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GuardianHomeState())
    val state = _state.asStateFlow()

    private val rtdbListeners = mutableListOf<Pair<DatabaseReference, ValueEventListener>>()

    init {
        collectSessionData()
        attachRtdbListeners()
    }

    private fun collectSessionData() {
        viewModelScope.launch {
            sessionRepository.elderName.collect { name ->
                _state.update { it.copy(elderName = name ?: "") }
            }
        }
        viewModelScope.launch {
            sessionRepository.elderPhotoUrl.collect { url ->
                _state.update { it.copy(elderPhotoUrl = url) }
            }
        }
        viewModelScope.launch {
            sessionRepository.guardianPhotoUrl.collect { url ->
                _state.update { it.copy(guardianPhotoUrl = url) }
            }
        }
    }

    private fun attachRtdbListeners() {
        viewModelScope.launch {
            val guardianUid = firebaseAuthDataSource.getCurentUserUid() ?: return@launch
            val linkResult = firestoreLinkDataSource.getLinkStatus(guardianUid)
            val elderUid = when (linkResult) {
                is Result.Success -> linkResult.data.linkedUid ?: return@launch
                else -> return@launch
            }

            val elderRef = firebaseDatabase.reference.child(elderUid)
            _state.update { it.copy(isLoading = false) }

            elderRef.listenString("battery_level") { value ->
                _state.update { it.copy(batteryLevel = value.toIntOrNull() ?: it.batteryLevel) }
            }
            elderRef.listenString("battery_isCharging") { value ->
                _state.update { it.copy(isCharging = value.toBooleanStrictOrNull() ?: it.isCharging) }
            }
            elderRef.listenLong("battery_lastSeen") { value ->
                _state.update { it.copy(lastBatterySeen = value) }
            }
            elderRef.listenString("location_lat") { value ->
                _state.update { it.copy(locationLat = value.toDoubleOrNull() ?: it.locationLat) }
            }
            elderRef.listenString("location_long") { value ->
                _state.update { it.copy(locationLong = value.toDoubleOrNull() ?: it.locationLong) }
            }
            elderRef.listenLong("location_lastSeen") { value ->
                _state.update { it.copy(lastLocationSeen = value) }
            }
        }
    }

    private fun DatabaseReference.listenString(path: String, onValue: (String) -> Unit) {
        val childRef = child(path)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(String::class.java) ?: return
                onValue(value)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        childRef.addValueEventListener(listener)
        rtdbListeners.add(childRef to listener)
    }

    private fun DatabaseReference.listenLong(path: String, onValue: (Long) -> Unit) {
        val childRef = child(path)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Long::class.java) ?: return
                onValue(value)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        childRef.addValueEventListener(listener)
        rtdbListeners.add(childRef to listener)
    }

    fun onAction(action: GuardianHomeAction) {
        when (action) {
            GuardianHomeAction.OnConfirmReminder -> {
                // TODO: mark reminder confirmed once reminders feature is built
            }
            GuardianHomeAction.OnSeeAllHistory -> {
                // TODO: navigate to activity log once built
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        rtdbListeners.forEach { (ref, listener) -> ref.removeEventListener(listener) }
        rtdbListeners.clear()
    }
}

fun Long.toLastActiveText(): String {
    if (this == 0L) return "—"
    val now = System.currentTimeMillis()
    val diffMinutes = (now - this) / 60_000
    return when {
        diffMinutes < 1 -> "Just now"
        diffMinutes < 60 -> "$diffMinutes mins ago"
        else -> "${diffMinutes / 60} hrs ago"
    }
}
