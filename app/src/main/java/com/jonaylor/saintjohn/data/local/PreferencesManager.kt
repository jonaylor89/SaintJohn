package com.jonaylor.saintjohn.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_preferences")

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val MONOCHROME_ENABLED = booleanPreferencesKey("monochrome_enabled")
        val SYSTEM_GRAYSCALE_ENABLED = booleanPreferencesKey("system_grayscale_enabled")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "BLLOC"
    }

    val monochromeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MONOCHROME_ENABLED] ?: true
    }

    val systemGrayscaleEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SYSTEM_GRAYSCALE_ENABLED] ?: false
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FIRST_LAUNCH] ?: true
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setMonochromeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MONOCHROME_ENABLED] = enabled
        }
    }

    suspend fun setSystemGrayscaleEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SYSTEM_GRAYSCALE_ENABLED] = enabled
        }
    }

    suspend fun setFirstLaunch(isFirst: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH] = isFirst
        }
    }
}
