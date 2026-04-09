package com.biprangshu.guardiansathi.Global.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.domain.SessionRepository
import com.biprangshu.guardiansathi.Global.domain.AuthRepository
import com.biprangshu.guardiansathi.Global.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface LoginAction {
    data object OnGoogleSignInClick : LoginAction
    data class OnGoogleSignInResult(val idToken: String?, val errorMessage: String?) : LoginAction
}

sealed interface LoginEvent {
    data object NavigateToRegistration : LoginEvent
    data class ShowError(val message: String) : LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnGoogleSignInClick -> {
                // UI will handle the actual intent launching via Credential Manager
                _state.update { it.copy(isLoading = true, error = null) }
            }
            is LoginAction.OnGoogleSignInResult -> {
                handleGoogleSignInResult(action.idToken, action.errorMessage)
            }
        }
    }

    private fun handleGoogleSignInResult(idToken: String?, errorMessage: String?) {
        if (idToken != null) {
            viewModelScope.launch {
                val result = authRepository.signInWithGoogle(idToken)
                when (result) {
                    is Result.Success -> {
                        sessionRepository.setLoggedIn(true)
                        _state.update { it.copy(isLoading = false) }
                        _events.emit(LoginEvent.NavigateToRegistration)
                    }
                    is Result.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.error.toString()) }
                        _events.emit(LoginEvent.ShowError("Sign in failed: ${result.error}"))
                    }
                }
            }
        } else {
            _state.update { it.copy(isLoading = false, error = errorMessage) }
            viewModelScope.launch {
                _events.emit(LoginEvent.ShowError(errorMessage ?: "Unknown error occurred"))
            }
        }
    }
}