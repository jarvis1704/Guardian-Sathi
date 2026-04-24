package com.biprangshu.guardiansathi.Global.Guardian.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.Guardian.data.GuardianAlert
import com.biprangshu.guardiansathi.Global.Guardian.data.GuardianAlertsRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GuardianAlertViewModel @Inject constructor(
    private val repository: GuardianAlertsRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _alerts = MutableStateFlow<List<GuardianAlert>>(emptyList())
    val alerts: StateFlow<List<GuardianAlert>> = _alerts.asStateFlow()

    init {
        fetchAlerts()
    }

    private fun fetchAlerts() {
        val uid = firebaseAuth.uid ?: return
        viewModelScope.launch {
            repository.getAlerts(uid).collect { newAlerts ->
                _alerts.value = newAlerts
            }
        }
    }
}
