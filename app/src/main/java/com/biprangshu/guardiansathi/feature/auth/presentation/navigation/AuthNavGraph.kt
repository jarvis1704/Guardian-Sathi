package com.biprangshu.guardiansathi.feature.auth.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.biprangshu.guardiansathi.core.domain.SessionRepository
import com.biprangshu.guardiansathi.feature.auth.presentation.login.LoginRoot
import com.biprangshu.guardiansathi.feature.auth.presentation.onboarding.OnboardingRoot
import com.biprangshu.guardiansathi.feature.auth.presentation.registration.RegistrationRoot
import com.biprangshu.guardiansathi.feature.auth.presentation.splash.SplashRoot
import kotlinx.coroutines.launch

@Composable
fun AuthNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = SplashRoute) {
        composable<SplashRoute> {
            SplashRoot(
                onNavigateToLanguageSelection = {
                    navController.navigate(LanguageSelectionRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(OnboardingRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToRegistration = {
                    navController.navigate(RegistrationRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(MainRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<LanguageSelectionRoute> {
            // Using a simple placeholder instead of the original one for demo,
            // or we could integrate the existing LanguageSelectionPage here if needed.
            // But we need to update the data store. Let's just create an inline one for now.
            val sessionRepo = hiltViewModel<com.biprangshu.guardiansathi.feature.auth.presentation.splash.SplashViewModel>() // We don't inject session repo directly into composable ideally
            // Instead we should make a LanguageSelectionViewModel.
            LanguageSelectionPlaceholderRoot(
                onNavigateNext = {
                    navController.navigate(OnboardingRoute) {
                        popUpTo(LanguageSelectionRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<OnboardingRoute> {
            OnboardingRoot(
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<LoginRoute> {
            LoginRoot(
                onNavigateToRegistration = {
                    navController.navigate(RegistrationRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<RegistrationRoute> {
            RegistrationRoot(
                onNavigateToMain = {
                    navController.navigate(MainRoute) {
                        popUpTo(RegistrationRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<MainRoute> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Main App Flow (Placeholder)")
            }
        }
    }
}
