package com.biprangshu.guardiansathi.Global.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.core.domain.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

sealed interface SplashEvent {
    data object NavigateToLanguageSelection : SplashEvent
    data object NavigateToOnboarding : SplashEvent
    data object NavigateToLogin : SplashEvent
    data object NavigateToRegistration : SplashEvent
    data object NavigateToMain : SplashEvent
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
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
                else -> SplashEvent.NavigateToMain
            }
            _events.emit(event)
        }
    }
}