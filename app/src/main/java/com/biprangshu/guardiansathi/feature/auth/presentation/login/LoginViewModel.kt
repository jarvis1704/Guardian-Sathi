package com.biprangshu.guardiansathi.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.core.domain.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false
)

sealed interface LoginAction {
    data object OnLoginClick : LoginAction
}

sealed interface LoginEvent {
    data object NavigateToRegistration : LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.OnLoginClick -> performMockLogin()
        }
    }

    private fun performMockLogin() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Mock login delay
            kotlinx.coroutines.delay(1000)
            
            sessionRepository.setLoggedIn(true)
            _state.update { it.copy(isLoading = false) }
            _events.emit(LoginEvent.NavigateToRegistration)
        }
    }
}
