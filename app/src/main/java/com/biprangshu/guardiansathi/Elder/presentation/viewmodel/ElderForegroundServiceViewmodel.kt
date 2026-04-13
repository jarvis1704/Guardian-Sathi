package com.biprangshu.guardiansathi.Elder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.biprangshu.guardiansathi.Elder.data.ElderForegroundServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ElderServiceState(
    val isServiceRunning: Boolean = false
)

@HiltViewModel
class ElderForegroundServiceViewmodel @Inject constructor(
    private val repository: ElderForegroundServiceRepository
) : ViewModel() {

    private val _serviceState = MutableStateFlow(ElderServiceState())
    val serviceState = _serviceState.asStateFlow()

    init {
        checkServiceStatus()
    }

    fun checkServiceStatus() {
        val running = repository.isServiceRunning()
        _serviceState.update {
            it.copy(isServiceRunning = running)
        }
    }

    fun toggleService() {
        if (_serviceState.value.isServiceRunning) {
            repository.stopForegroundService()
        } else {
            repository.startForegroundService()
        }
        checkServiceStatus()
    }

    fun startService() {
        repository.startForegroundService()
        checkServiceStatus()
    }

    fun stopService() {
        repository.stopForegroundService()
        checkServiceStatus()
    }
}
