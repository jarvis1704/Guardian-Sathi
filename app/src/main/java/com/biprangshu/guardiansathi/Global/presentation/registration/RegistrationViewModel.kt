package com.biprangshu.guardiansathi.Global.presentation.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.core.data.FirebaseAuthDataSource
import com.biprangshu.guardiansathi.Global.core.domain.AuthRepository
import com.biprangshu.guardiansathi.Global.domain.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistrationState(
    val selectedRole: String? = null, // "ELDER" or "GUARDIAN"
    val isLoading: Boolean = false
)

sealed interface RegistrationAction {
    data class OnRoleSelect(val role: String) : RegistrationAction
    data object OnSubmitClick : RegistrationAction
}

sealed interface RegistrationEvent {
    data object NavigateToMain : RegistrationEvent
}

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val firebaseAuthDataSource: FirebaseAuthDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<RegistrationEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: RegistrationAction) {
        when (action) {
            is RegistrationAction.OnRoleSelect -> {
                _state.update { it.copy(selectedRole = action.role) }
            }
            RegistrationAction.OnSubmitClick -> {
                val role = _state.value.selectedRole
                if (role != null) {
                    performRegistration(role)
                }
            }
        }
    }

    private fun performRegistration(role: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Sync current user info before transitioning
            val name = firebaseAuthDataSource.getCurrentUserName()
            val photo = firebaseAuthDataSource.getCurrentUserPhotoUrl()
            when (role) {
                "ELDER" -> sessionRepository.setElderInfo(name, photo)
                "GUARDIAN" -> sessionRepository.setGuardianInfo(name, photo)
            }
            
            sessionRepository.setUserRole(role)
            authRepository.updateUserRole(role)
            
            _state.update { it.copy(isLoading = false) }
            _events.emit(RegistrationEvent.NavigateToMain)
        }
    }
}
