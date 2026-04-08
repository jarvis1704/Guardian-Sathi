package com.biprangshu.guardiansathi.feature.auth.presentation.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.biprangshu.guardiansathi.feature.auth.presentation.navigation.LanguageSelectionRoute
import com.biprangshu.guardiansathi.feature.auth.presentation.navigation.LoginRoute
import com.biprangshu.guardiansathi.feature.auth.presentation.navigation.MainRoute
import com.biprangshu.guardiansathi.feature.auth.presentation.navigation.OnboardingRoute
import com.biprangshu.guardiansathi.feature.auth.presentation.navigation.RegistrationRoute

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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
