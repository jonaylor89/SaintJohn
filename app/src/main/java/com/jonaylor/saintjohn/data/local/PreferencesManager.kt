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
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val ANTHROPIC_API_KEY = stringPreferencesKey("anthropic_api_key")
        val GOOGLE_API_KEY = stringPreferencesKey("google_api_key")
        val SELECTED_LLM_PROVIDER = stringPreferencesKey("selected_llm_provider")
        val SELECTED_OPENAI_MODEL = stringPreferencesKey("selected_openai_model")
        val SELECTED_ANTHROPIC_MODEL = stringPreferencesKey("selected_anthropic_model")
        val SELECTED_GOOGLE_MODEL = stringPreferencesKey("selected_google_model")
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")

        const val DEFAULT_SYSTEM_PROMPT = """You are a helpful assistant integrated into an Android launcher. You're speaking directly to the user on their phone.

Be concise—mobile screens are small. Get to the point quickly. Use short paragraphs and bullet points when listing things.

When writing code:
- Use markdown code blocks with language tags
- Keep examples minimal and runnable
- Explain only what's necessary

When answering questions:
- Lead with the answer, then explain if needed
- If you're unsure, say so briefly
- Don't hedge excessively or over-qualify

You can help with coding, writing, analysis, math, planning, and general questions. You have no access to the user's phone, apps, calendar, or location—you're a conversational assistant only.

Match the user's energy and formality. If they're casual, be casual. If they're technical, be technical."""
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

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
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

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
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

    val systemPrompt: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SYSTEM_PROMPT] ?: DEFAULT_SYSTEM_PROMPT
    }

    suspend fun setSystemPrompt(prompt: String) {
        context.dataStore.edit { preferences ->
            preferences[SYSTEM_PROMPT] = prompt
        }
    }
}
