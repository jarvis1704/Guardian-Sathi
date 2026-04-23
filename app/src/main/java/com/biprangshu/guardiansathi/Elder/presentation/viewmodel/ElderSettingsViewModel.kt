package com.biprangshu.guardiansathi.Elder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Elder.core.NotificationData
import com.biprangshu.guardiansathi.Elder.data.ElderFirebaseRepository
import com.biprangshu.guardiansathi.Global.core.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ElderSettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseRepository: ElderFirebaseRepository
): ViewModel() {

    fun logout(){
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun PanicSOS(){
        val newNotifData = NotificationData(
            packageName = "Guardian Saathi",
            appName = "Guardian Saathi",
            title = "Panic SOS",
            desc = "Elder clicked on Panic SOS and needs immediate assistance!",
            body = "Elder needs immediate assistance",
            timestamp = 0
        )
        firebaseRepository.sendNotificaitonToGuardian(newNotifData, false, false, "SOS")
    }

    fun FallSOS(){
        val newNotifData = NotificationData(
            packageName = "Guardian Saathi",
            appName = "Guardian Saathi",
            title = "Fall Detection SOS",
            desc = "Elder's device fell from a high place, needs immediate assistance!",
            body = "Elder needs immediate assistance",
            timestamp = 0
        )
        firebaseRepository.sendNotificaitonToGuardian(newNotifData, false, false, "SOS")
    }
}