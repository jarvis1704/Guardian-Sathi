package com.biprangshu.guardiansathi.feature.auth.presentation.onboarding

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

data class OnboardingState(
    val currentPage: Int = 0,
    val isLastPage: Boolean = false
)

sealed interface OnboardingAction {
    data object OnNextClick : OnboardingAction
    data object OnSkipClick : OnboardingAction
}

sealed interface OnboardingEvent {
    data object NavigateToLogin : OnboardingEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.OnNextClick -> {
                if (state.value.isLastPage) {
                    completeOnboarding()
                } else {
                    _state.update { 
                        val nextPage = it.currentPage + 1
                        it.copy(
                            currentPage = nextPage,
                            isLastPage = nextPage == 2 // Assuming 3 pages total (0, 1, 2)
                        ) 
                    }
                }
            }
            OnboardingAction.OnSkipClick -> {
                completeOnboarding()
            }
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            sessionRepository.setOnboardingCompleted(true)
            _events.emit(OnboardingEvent.NavigateToLogin)
        }
    }
}
