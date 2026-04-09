package com.biprangshu.guardiansathi.Global.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavController
import com.biprangshu.guardiansathi.Global.presentation.login.LoginRoot
import com.biprangshu.guardiansathi.Global.presentation.onboarding.OnboardingRoot
import com.biprangshu.guardiansathi.Global.presentation.registration.RegistrationRoot
import com.biprangshu.guardiansathi.Global.presentation.splash.SplashRoot
import com.biprangshu.guardiansathi.Global.presentation.splash.SplashViewModel
import com.biprangshu.guardiansathi.Global.presentation.onboarding.LanguageSelectionPage
import com.biprangshu.guardiansathi.Global.presentation.splash.LoadingPage
import com.biprangshu.guardiansathi.Global.presentation.ui.components.OstrichAlgorithm
import com.biprangshu.guardiansathi.Global.presentation.ui.components.errorMessage
import com.biprangshu.guardiansathi.Global.presentation.ui.components.isErrorAlert
import com.biprangshu.guardiansathi.Global.presentation.navigation.LanguageSelectionRoute
import com.biprangshu.guardiansathi.Global.presentation.navigation.LanguageSelectionViewModel
import com.biprangshu.guardiansathi.Global.presentation.navigation.LoginRoute
import com.biprangshu.guardiansathi.Global.presentation.navigation.MainRoute
import com.biprangshu.guardiansathi.Global.presentation.navigation.OnboardingRoute
import com.biprangshu.guardiansathi.Global.presentation.navigation.RegistrationRoute
import com.biprangshu.guardiansathi.Global.presentation.navigation.SplashRoute

import com.biprangshu.guardiansathi.Global.presentation.navigation.RegistrationGraph

// ROUTES (keep centralized)

@Composable
fun AppNav(
    navController: NavHostController
) {
    //for errorMessage
    LaunchedEffect(key1 = errorMessage) {
        if (errorMessage !="" && errorMessage !in OstrichAlgorithm){
            isErrorAlert =true
            Log.d("apperror", errorMessage)
        }
    }

    NavHost(
        navController = navController,
        startDestination = RegistrationGraph
    ) {

        // 🔹 Registration Flow
        navigation<RegistrationGraph>(
            startDestination = SplashRoute
        ) {
            registrationNav(navController)
        }

        // 🔹 Elder Flow
        navigation(
            startDestination = "elder_home",
            route = Routes.ELDER
        ) {
            elderNav(navController)
        }

        // 🔹 Guardian Flow
        navigation(
            startDestination = "guardian_home",
            route = Routes.GUARDIAN
        ) {
            guardianNav(navController)
        }
    }
}

fun NavGraphBuilder.registrationNav(navController: NavController) {

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
        val viewModel = hiltViewModel<LanguageSelectionViewModel>()
        val context = LocalContext.current
        LanguageSelectionPage(
            onContinue = { languageCode ->
                viewModel.onLanguageSelected(context, languageCode)
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

fun NavGraphBuilder.elderNav(navController: NavController) {

    composable("elder_home") {
//        ElderHomeScreen()
    }
}

fun NavGraphBuilder.guardianNav(navController: NavController) {

    composable("guardian_home") {
//        GuardianHomeScreen()
    }
}