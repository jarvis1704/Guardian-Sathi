package com.biprangshu.guardiansathi.Guardian.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.core.data.FirebaseAuthDataSource
import com.biprangshu.guardiansathi.Global.core.data.FirestoreUserDataSource
import com.biprangshu.guardiansathi.Global.core.domain.DataError
import com.biprangshu.guardiansathi.Global.core.domain.LinkRepository
import com.biprangshu.guardiansathi.Global.core.domain.Result
import com.biprangshu.guardiansathi.Global.domain.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LinkElderState(
    val codeInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showScanner: Boolean = false
)

sealed interface LinkElderAction {
    data class OnCodeInputChange(val value: String) : LinkElderAction
    data object OnSubmitCode : LinkElderAction
    data class OnQrScanned(val rawValue: String) : LinkElderAction
    data object OnToggleScanner : LinkElderAction
}

sealed interface LinkElderEvent {
    data object NavigateToGuardianHome : LinkElderEvent
    data class ShowConnectionSuccess(
        val connectedName: String,
        val myPhotoUrl: String,
        val connectedPhotoUrl: String
    ) : LinkElderEvent
    data class ShowError(val message: String) : LinkElderEvent
}

@HiltViewModel
class LinkElderViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    private val sessionRepository: SessionRepository,
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(LinkElderState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<LinkElderEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: LinkElderAction) {
        when (action) {
            is LinkElderAction.OnCodeInputChange ->
                _state.update { it.copy(codeInput = action.value, error = null) }

            is LinkElderAction.OnSubmitCode ->
                submitCode(_state.value.codeInput.trim().uppercase())

            is LinkElderAction.OnQrScanned -> {
                // Parse "guardiansathi://link?code=SAATHI-XXXXXX"
                val code = action.rawValue
                    .removePrefix("guardiansathi://link?code=")
                    .trim()
                _state.update { it.copy(showScanner = false) }
                submitCode(code)
            }

            is LinkElderAction.OnToggleScanner ->
                _state.update { it.copy(showScanner = !it.showScanner, error = null) }
        }
    }

    private fun submitCode(code: String) {
        if (code.isBlank()) {
            _state.update { it.copy(error = "Please enter a code") }
            return
        }
        val guardianUid = firebaseAuthDataSource.getCurentUserUid() ?: run {
            viewModelScope.launch { _events.emit(LinkElderEvent.ShowError("Not logged in")) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = linkRepository.linkViaCode(code, guardianUid)) {
                is Result.Success -> {
                    sessionRepository.setLinked(true)
                    _state.update { it.copy(isLoading = false) }
                    val linkedUid = when (val s = linkRepository.getLinkStatus(guardianUid)) {
                        is Result.Success -> s.data.linkedUid
                        else -> null
                    }
                    if (linkedUid != null) {
                        val connectedUser = firestoreUserDataSource.getUserById(linkedUid)
                        val myPhotoUrl = firebaseAuthDataSource.getCurrentUserPhotoUrl() ?: ""
                        val connectedName = (connectedUser as? Result.Success)?.data?.displayName ?: ""
                        val connectedPhoto = (connectedUser as? Result.Success)?.data?.photoUrl ?: ""
                        //datastore storing locally
                        sessionRepository.setGuardianInfo(connectedName, connectedPhoto)
                        //elder info can also be implemented in the future if needed
                        _events.emit(LinkElderEvent.ShowConnectionSuccess(connectedName, myPhotoUrl, connectedPhoto))
                    } else {
                        _events.emit(LinkElderEvent.NavigateToGuardianHome)
                    }
                }
                is Result.Error -> {
                    val message = when (result.error) {
                        DataError.Network.NOT_FOUND -> "Code not found. Check and try again."
                        DataError.Network.REQUEST_TIMEOUT -> "Code has expired. Ask your Elder for a new one."
                        else -> "Linking failed. Please try again."
                    }
                    _state.update { it.copy(isLoading = false, error = message) }
                    _events.emit(LinkElderEvent.ShowError(message))
                }
            }
        }
    }
}
