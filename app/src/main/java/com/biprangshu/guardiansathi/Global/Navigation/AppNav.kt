package com.biprangshu.guardiansathi.Global.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavController
import com.biprangshu.guardiansathi.Global.ui.LanguageSelectionPage
import com.biprangshu.guardiansathi.Global.ui.LoadingPage

// ROUTES (keep centralized)

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
            startDestination = NavScreensObj_Registration.LOADINGPAGE,
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

    composable(
        NavScreensObj_Registration.LOADINGPAGE
    ) {
        LoadingPage(
            goto_selectlanguage = {
                navController.navigate(NavScreensObj_Registration.SELECTLANGUAGEPAGE)
            }
        )
    }
    composable(NavScreensObj_Registration.SELECTLANGUAGEPAGE) {
        LanguageSelectionPage(
            goto_loading = {
                navController.navigate(NavScreensObj_Registration.LOADINGPAGE)
            }
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