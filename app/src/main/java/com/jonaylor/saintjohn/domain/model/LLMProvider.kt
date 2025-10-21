package com.jonaylor.saintjohn.domain.model

enum class LLMProvider(val displayName: String, val apiKeyName: String) {
    OPENAI("OpenAI", "OpenAI API Key"),
    ANTHROPIC("Anthropic", "Anthropic API Key"),
    GOOGLE("Google", "Google API Key")
}
