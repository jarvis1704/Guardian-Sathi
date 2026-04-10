package com.biprangshu.guardiansathi.Global.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.biprangshu.guardiansathi.Elder.presentation.screens.LinkGuardianRoot
import com.biprangshu.guardiansathi.Guardian.presentation.screens.LinkElderRoot
import com.biprangshu.guardiansathi.Global.presentation.login.LoginRoot
import com.biprangshu.guardiansathi.Global.presentation.onboarding.OnboardingRoot
import com.biprangshu.guardiansathi.Global.presentation.onboarding.LanguageSelectionPage
import com.biprangshu.guardiansathi.Global.presentation.registration.RegistrationRoot
import com.biprangshu.guardiansathi.Global.presentation.splash.SplashRoot
import com.biprangshu.guardiansathi.Global.presentation.ui.components.OstrichAlgorithm
import com.biprangshu.guardiansathi.Global.presentation.ui.components.errorMessage
import com.biprangshu.guardiansathi.Global.presentation.ui.components.isErrorAlert

@Composable
fun AppNav(
    navController: NavHostController
) {
    LaunchedEffect(key1 = errorMessage) {
        if (errorMessage != "" && errorMessage !in OstrichAlgorithm) {
            isErrorAlert = true
            Log.d("apperror", errorMessage)
        }
    }

    NavHost(
        navController = navController,
        startDestination = RegistrationGraph
    ) {

        // Registration / onboarding flow
        navigation<RegistrationGraph>(startDestination = SplashRoute) {
            registrationNav(navController)
        }

        // Elder flow
        navigation<ElderGraph>(startDestination = LinkGuardianRoute) {
            elderNav(navController)
        }

        // Guardian flow
        navigation<GuardianGraph>(startDestination = LinkElderRoute) {
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
                // Safety fallback — should not be reached in normal flow
                navController.navigate(RegistrationRoute) {
                    popUpTo(SplashRoute) { inclusive = true }
                }
            },
            onNavigateToLinkGuardian = {
                navController.navigate(ElderGraph) {
                    popUpTo(RegistrationGraph) { inclusive = true }
                }
            },
            onNavigateToLinkElder = {
                navController.navigate(GuardianGraph) {
                    popUpTo(RegistrationGraph) { inclusive = true }
                }
            },
            onNavigateToElderHome = {
                navController.navigate(ElderHomeRoute) {
                    popUpTo(RegistrationGraph) { inclusive = true }
                }
            },
            onNavigateToGuardianHome = {
                navController.navigate(GuardianHomeRoute) {
                    popUpTo(RegistrationGraph) { inclusive = true }
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
                // After role selection, clear entire back stack and re-run Splash
                // so SplashViewModel checks link status and routes correctly.
                navController.navigate(SplashRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    composable<MainRoute> {
        // Safety fallback
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

fun NavGraphBuilder.elderNav(navController: NavController) {

    composable<LinkGuardianRoute> {
        LinkGuardianRoot(
            onNavigateToElderHome = {
                navController.navigate(ElderHomeRoute) {
                    popUpTo(LinkGuardianRoute) { inclusive = true }
                }
            }
        )
    }

    composable<ElderHomeRoute> {
        ElderHomePlaceholder()
    }
}

fun NavGraphBuilder.guardianNav(navController: NavController) {

    composable<LinkElderRoute> {
        LinkElderRoot(
            onNavigateToGuardianHome = {
                navController.navigate(GuardianHomeRoute) {
                    popUpTo(LinkElderRoute) { inclusive = true }
                }
            }
        )
    }

    composable<GuardianHomeRoute> {
        GuardianHomePlaceholder()
    }
}

@Composable
private fun ElderHomePlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Elder Home — Connected!")
    }
}

@Composable
private fun GuardianHomePlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Guardian Home — Connected!")
    }
}
