package com.biprangshu.guardiansathi.Global.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.biprangshu.guardiansathi.Global.domain.SessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

@Singleton
class UserSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SessionRepository {

    private object PreferencesKeys {
        val IS_LANGUAGE_SELECTED = booleanPreferencesKey("is_language_selected")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ROLE = stringPreferencesKey("user_role")
    }

    override val isLanguageSelected: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_LANGUAGE_SELECTED] ?: false
    }

    override val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false
    }

    override val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
    }

    override val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_ROLE]
    }

    override suspend fun setLanguageSelected(selected: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LANGUAGE_SELECTED] = selected
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    override suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = loggedIn
        }
    }

    override suspend fun setUserRole(role: String?) {
        context.dataStore.edit { preferences ->
            if (role == null) {
                preferences.remove(PreferencesKeys.USER_ROLE)
            } else {
                preferences[PreferencesKeys.USER_ROLE] = role
            }
        }
    }
}
