package com.biprangshu.guardiansathi.Global.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object RegistrationGraph

@Serializable
data object SplashRoute

@Serializable
data object LanguageSelectionRoute

@Serializable
data object OnboardingRoute

@Serializable
data object LoginRoute

@Serializable
data object RegistrationRoute

@Serializable
data object MainRoute

// Elder graph
@Serializable
data object ElderGraph

@Serializable
data object LinkGuardianRoute

@Serializable
data object ElderHomeRoute

@Serializable
data object EmergencyContactsRoute

@Serializable
data object PanicSOSRoute



// Guardian graph
@Serializable
data object GuardianGraph

@Serializable
data object LinkElderRoute

@Serializable
data object GuardianHomeRoute
