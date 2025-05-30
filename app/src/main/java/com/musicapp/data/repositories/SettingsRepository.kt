package com.musicapp.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.musicapp.data.models.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val ALLOW_EXPLICIT_KEY = stringPreferencesKey("allowExplicit")
    }

    val theme: Flow<Theme> = dataStore.data.map { preferences ->
        try {
            Theme.valueOf(preferences[THEME_KEY] ?: "Default")
        } catch (_: Exception) {
            Theme.Default
        }
    }

    val allowExplicit: Flow<Boolean> = dataStore.data.map { it[ALLOW_EXPLICIT_KEY]?.toBoolean() != false }

    suspend fun setTheme(theme: Theme) = dataStore.edit { preferences ->
        preferences[THEME_KEY] = theme.toString()
    }

    suspend fun setExplicit(showExplicit: Boolean) = dataStore.edit { preferences ->
        preferences[ALLOW_EXPLICIT_KEY] = showExplicit.toString()
    }
}