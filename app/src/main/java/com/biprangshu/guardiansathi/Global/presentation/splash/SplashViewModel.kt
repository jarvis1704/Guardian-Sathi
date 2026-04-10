package com.biprangshu.guardiansathi.Global.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.core.data.FirebaseAuthDataSource
import com.biprangshu.guardiansathi.Global.core.domain.LinkRepository
import com.biprangshu.guardiansathi.Global.core.domain.Result
import com.biprangshu.guardiansathi.Global.domain.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashEvent {
    data object NavigateToLanguageSelection : SplashEvent
    data object NavigateToOnboarding : SplashEvent
    data object NavigateToLogin : SplashEvent
    data object NavigateToRegistration : SplashEvent
    data object NavigateToMain : SplashEvent
    data object NavigateToLinkGuardian : SplashEvent
    data object NavigateToLinkElder : SplashEvent
    data object NavigateToElderHome : SplashEvent
    data object NavigateToGuardianHome : SplashEvent
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val linkRepository: LinkRepository,
    private val firebaseAuthDataSource: FirebaseAuthDataSource
) : ViewModel() {

    private val _events = MutableSharedFlow<SplashEvent>()
    val events = _events.asSharedFlow()

    init {
        determineNextScreen()
    }

    private fun determineNextScreen() {
        viewModelScope.launch {
            delay(1000)
            val isLanguageSelected = sessionRepository.isLanguageSelected.first()
            val hasCompletedOnboarding = sessionRepository.hasCompletedOnboarding.first()
            val isLoggedIn = sessionRepository.isLoggedIn.first()
            val userRole = sessionRepository.userRole.first()

            val event = when {
                !isLanguageSelected -> SplashEvent.NavigateToLanguageSelection
                !hasCompletedOnboarding -> SplashEvent.NavigateToOnboarding
                !isLoggedIn -> SplashEvent.NavigateToLogin
                userRole == null -> SplashEvent.NavigateToRegistration
                else -> {
                    val uid = firebaseAuthDataSource.getCurentUserUid()
                        ?: return@launch _events.emit(SplashEvent.NavigateToLogin)

                    // Fast path: check DataStore cache first
                    val cachedLinked = sessionRepository.isLinked.first()
                    if (cachedLinked) {
                        roleToHomeEvent(userRole)
                    } else {
                        // Verify against Firestore (handles reinstall / new device)
                        when (val result = linkRepository.getLinkStatus(uid)) {
                            is Result.Success -> {
                                if (result.data.isLinked) {
                                    sessionRepository.setLinked(true)
                                    roleToHomeEvent(userRole)
                                } else {
                                    roleToLinkEvent(userRole)
                                }
                            }
                            is Result.Error -> {
                                // Network failure: fall back to unlinked flow
                                roleToLinkEvent(userRole)
                            }
                        }
                    }
                }
            }
            _events.emit(event)
        }
    }

    private fun roleToHomeEvent(role: String): SplashEvent = when (role) {
        "ELDER" -> SplashEvent.NavigateToElderHome
        "GUARDIAN" -> SplashEvent.NavigateToGuardianHome
        else -> SplashEvent.NavigateToLogin
    }

    private fun roleToLinkEvent(role: String): SplashEvent = when (role) {
        "ELDER" -> SplashEvent.NavigateToLinkGuardian
        "GUARDIAN" -> SplashEvent.NavigateToLinkElder
        else -> SplashEvent.NavigateToLogin
    }
}
