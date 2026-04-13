package com.biprangshu.guardiansathi.Elder.presentation.viewmodel

import android.telephony.emergency.EmergencyNumber
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class EmergencyNumbersState(
    val isFetching: Boolean = false,
    val emergencyNumbers: List<EmergencyNumber> = emptyList()
)

@HiltViewModel
class EmergencyNumbersViewmodel @Inject constructor(

): ViewModel() {
    private val _emergencyNumbersState = MutableStateFlow(EmergencyNumbersState())
    val emergencyNumbersState = _emergencyNumbersState.asStateFlow()
}