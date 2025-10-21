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
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val ANTHROPIC_API_KEY = stringPreferencesKey("anthropic_api_key")
        val GOOGLE_API_KEY = stringPreferencesKey("google_api_key")
        val SELECTED_LLM_PROVIDER = stringPreferencesKey("selected_llm_provider")
        val SELECTED_OPENAI_MODEL = stringPreferencesKey("selected_openai_model")
        val SELECTED_ANTHROPIC_MODEL = stringPreferencesKey("selected_anthropic_model")
        val SELECTED_GOOGLE_MODEL = stringPreferencesKey("selected_google_model")
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

    val openaiApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[OPENAI_API_KEY] ?: ""
    }

    val anthropicApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ANTHROPIC_API_KEY] ?: ""
    }

    val googleApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[GOOGLE_API_KEY] ?: ""
    }

    val selectedLLMProvider: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_LLM_PROVIDER] ?: "ANTHROPIC"
    }

    suspend fun setOpenAIApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[OPENAI_API_KEY] = key
        }
    }

    suspend fun setAnthropicApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[ANTHROPIC_API_KEY] = key
        }
    }

    suspend fun setGoogleApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[GOOGLE_API_KEY] = key
        }
    }

    suspend fun setSelectedLLMProvider(provider: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_LLM_PROVIDER] = provider
        }
    }

    val selectedOpenAIModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_OPENAI_MODEL] ?: "gpt-4o-mini"
    }

    val selectedAnthropicModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_ANTHROPIC_MODEL] ?: "claude-sonnet-4-5-20250929"
    }

    val selectedGoogleModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_GOOGLE_MODEL] ?: "gemini-2.5-flash"
    }

    suspend fun setSelectedOpenAIModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_OPENAI_MODEL] = model
        }
    }

    suspend fun setSelectedAnthropicModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_ANTHROPIC_MODEL] = model
        }
    }

    suspend fun setSelectedGoogleModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_GOOGLE_MODEL] = model
        }
    }
}
