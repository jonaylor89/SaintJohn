package com.jonaylor.saintjohn.domain.model

enum class LLMProvider(val displayName: String, val apiKeyName: String) {
    OPENAI("OpenAI (GPT-4)", "OpenAI API Key"),
    ANTHROPIC("Anthropic (Claude)", "Anthropic API Key"),
    GOOGLE("Google (Gemini)", "Google API Key")
}
