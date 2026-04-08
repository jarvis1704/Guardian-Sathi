package com.biprangshu.guardiansathi.Global.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavController
import com.biprangshu.guardiansathi.Global.ui.LanguageSelectionPage

// ROUTES (keep centralized)
object Routes {
    const val REGISTRATION = "registration"
    const val ELDER = "elder"
    const val GUARDIAN = "guardian"
}

@Composable
fun AppNav(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.REGISTRATION
    ) {

        // 🔹 Registration Flow
        navigation(
            startDestination = "language",
            route = Routes.REGISTRATION
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

    composable("language") {
        LanguageSelectionPage(
//            onContinue = {
//                navController.navigate("role_selection")
//            }
        )
    }

//    composable("role_selection") {
//        RoleSelectionPage(
//            onElder = {
//                navController.navigate(Routes.ELDER) {
//                    popUpTo(Routes.REGISTRATION) { inclusive = true }
//                }
//            },
//            onGuardian = {
//                navController.navigate(Routes.GUARDIAN) {
//                    popUpTo(Routes.REGISTRATION) { inclusive = true }
//                }
//            }
//        )
//    }
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