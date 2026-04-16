package com.biprangshu.guardiansathi.Elder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.core.data.FirebaseAuthDataSource
import com.biprangshu.guardiansathi.Global.core.data.FirestoreUserDataSource
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

data class LinkGuardianState(
    val linkCode: String? = null,
    val qrContent: String? = null,
    val isWaiting: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface LinkGuardianEvent {
    data object NavigateToElderHome : LinkGuardianEvent
    data class ShowConnectionSuccess(
        val connectedName: String,
        val myPhotoUrl: String,
        val connectedPhotoUrl: String
    ) : LinkGuardianEvent
}

sealed interface LinkGuardianAction {
    data object OnRetryGenerateCode : LinkGuardianAction
}

@HiltViewModel
class LinkGuardianViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    private val sessionRepository: SessionRepository,
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(LinkGuardianState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<LinkGuardianEvent>()
    val events = _events.asSharedFlow()

    init {
        generateCodeAndStartListening()
    }

    private fun generateCodeAndStartListening() {
        val uid = firebaseAuthDataSource.getCurentUserUid() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = linkRepository.generateLinkCode(uid)) {
                is Result.Success -> {
                    val code = result.data
                    _state.update {
                        it.copy(
                            linkCode = code,
                            qrContent = "guardiansathi://link?code=$code",
                            isLoading = false,
                            isWaiting = true
                        )
                    }
                    observeLinkStatus(uid)
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = "Failed to generate code. Check your connection.") }
                }
            }
        }
    }

    private fun observeLinkStatus(uid: String) {
        viewModelScope.launch {
            linkRepository.observeLinkStatus(uid).collect { linkStatus ->
                if (linkStatus.isLinked) {
                    sessionRepository.setLinked(true)
                    _state.update { it.copy(isWaiting = false) }
                    val linkedUid = linkStatus.linkedUid
                    if (linkedUid != null) {
                        val connectedUser = firestoreUserDataSource.getUserById(linkedUid)
                        val myPhotoUrl = firebaseAuthDataSource.getCurrentUserPhotoUrl() ?: ""
                        val connectedName = (connectedUser as? Result.Success)?.data?.displayName ?: ""
                        val connectedPhoto = (connectedUser as? Result.Success)?.data?.photoUrl ?: ""
                        // persist guardian info locally for Elder persona
                        sessionRepository.setGuardianInfo(connectedName, connectedPhoto)
                        _events.emit(LinkGuardianEvent.ShowConnectionSuccess(connectedName, myPhotoUrl, connectedPhoto))
                    } else {
                        _events.emit(LinkGuardianEvent.NavigateToElderHome)
                    }
                }
            }
        }
    }

    fun onAction(action: LinkGuardianAction) {
        when (action) {
            LinkGuardianAction.OnRetryGenerateCode -> generateCodeAndStartListening()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up orphaned linkCode when Elder leaves without being linked
        val code = _state.value.linkCode
        if (code != null && _state.value.isWaiting) {
            viewModelScope.launch {
                linkRepository.deleteLinkCode(code)
            }
        }
    }
}
