package com.biprangshu.guardiansathi.Global.presentation.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashRoot(
    onNavigateToLanguageSelection: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegistration: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SplashEvent.NavigateToLanguageSelection -> onNavigateToLanguageSelection()
                is SplashEvent.NavigateToOnboarding -> onNavigateToOnboarding()
                is SplashEvent.NavigateToLogin -> onNavigateToLogin()
                is SplashEvent.NavigateToRegistration -> onNavigateToRegistration()
                is SplashEvent.NavigateToMain -> onNavigateToMain()
            }
        }
    }

    SplashScreen()
}

@Composable
fun SplashScreen() {
    LoadingPage()
}