package com.biprangshu.guardiansathi.Global.domain

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val isLanguageSelected: Flow<Boolean>
    val hasCompletedOnboarding: Flow<Boolean>
    val isLoggedIn: Flow<Boolean>
    val userRole: Flow<String?>
    val isLinked: Flow<Boolean>

    val guardianName: Flow<String?>
    val guardianPhotoUrl: Flow<String?>
    //elder name for future usage
    val elderName: Flow<String?>
    val elderPhotoUrl: Flow<String?>

    suspend fun setLanguageSelected(selected: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setLoggedIn(loggedIn: Boolean)
    suspend fun setUserRole(role: String?)
    suspend fun setLinked(linked: Boolean)
}
