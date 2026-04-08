package com.biprangshu.guardiansathi.Global.domain

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val isLanguageSelected: Flow<Boolean>
    val hasCompletedOnboarding: Flow<Boolean>
    val isLoggedIn: Flow<Boolean>
    val userRole: Flow<String?>

    suspend fun setLanguageSelected(selected: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setLoggedIn(loggedIn: Boolean)
    suspend fun setUserRole(role: String?)
}
