package com.biprangshu.guardiansathi.Global.Guardian.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.core.domain.AuthRepository
import com.biprangshu.guardiansathi.Global.domain.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GuardianProfileState(
    val guardianName: String = "",
    val guardianPhotoUrl: String? = null,
    val elderName: String = "",
    val elderPhotoUrl: String? = null
)

sealed interface GuardianProfileAction {
    data object OnLogout : GuardianProfileAction
}

sealed interface GuardianProfileEvent {
    data object NavigateToSplash : GuardianProfileEvent
}

@HiltViewModel
class GuardianProfileViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GuardianProfileState())
    val state = _state.asStateFlow()

    private val _events = Channel<GuardianProfileEvent>()
    val events = _events.receiveAsFlow()

    init {
        collectSessionData()
    }

    private fun collectSessionData() {
        viewModelScope.launch {
            sessionRepository.guardianName.collect { name ->
                _state.update { it.copy(guardianName = name ?: "") }
            }
        }
        viewModelScope.launch {
            sessionRepository.guardianPhotoUrl.collect { url ->
                _state.update { it.copy(guardianPhotoUrl = url) }
            }
        }
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
    }

    fun onAction(action: GuardianProfileAction) {
        when (action) {
            GuardianProfileAction.OnLogout -> logout()
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            _events.send(GuardianProfileEvent.NavigateToSplash)
        }
    }
}
