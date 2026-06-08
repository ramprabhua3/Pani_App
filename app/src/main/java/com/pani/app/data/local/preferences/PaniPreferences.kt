package com.pani.app.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pani.app.domain.model.UserMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaniPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val userId: Flow<String?> = dataStore.data.map { it[Keys.USER_ID] }

    val userMode: Flow<UserMode?> = dataStore.data.map { prefs ->
        prefs[Keys.USER_MODE]?.let {
            runCatching { UserMode.valueOf(it) }.getOrNull()
        }
    }

    /** BCP-47 tag, e.g. "en", "hi", "ta", "te", "kn", "ml". Defaults to "en". */
    val locale: Flow<String> = dataStore.data.map { it[Keys.LOCALE] ?: "en" }

    val onboardingComplete: Flow<Boolean> = dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    suspend fun saveUserId(id: String) = dataStore.edit { it[Keys.USER_ID] = id }

    suspend fun saveUserMode(mode: UserMode) = dataStore.edit { it[Keys.USER_MODE] = mode.name }

    suspend fun saveLocale(locale: String) = dataStore.edit { it[Keys.LOCALE] = locale }

    suspend fun completeOnboarding() = dataStore.edit { it[Keys.ONBOARDING_DONE] = true }

    suspend fun clearSession() = dataStore.edit { it.clear() }

    private object Keys {
        val USER_ID          = stringPreferencesKey("user_id")
        val USER_MODE        = stringPreferencesKey("user_mode")
        val LOCALE           = stringPreferencesKey("locale")
        val ONBOARDING_DONE  = booleanPreferencesKey("onboarding_done")
    }
}
