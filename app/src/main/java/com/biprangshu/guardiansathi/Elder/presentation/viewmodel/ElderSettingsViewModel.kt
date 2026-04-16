package com.biprangshu.guardiansathi.Elder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.core.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ElderSettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {

    fun logout(){
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}