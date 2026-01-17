package com.jonaylor.saintjohn.data.repository

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for model ID filtering and sorting logic used in ChatRepository.
 */
class ModelFilteringTest {

    @Test
    fun `OpenAI chat models should be filtered correctly`() {
        val allModels = listOf(
            "gpt-4o",
            "gpt-4o-mini",
            "gpt-3.5-turbo",
            "o1",
            "o1-mini",
            "o3",
            "o4-mini",
            "text-embedding-ada-002",
            "whisper-1",
            "tts-1",
            "dall-e-3",
            "gpt-4o-realtime-preview"
        )
        
        val chatModels = allModels
            .filter { id ->
                id.startsWith("gpt-") || 
                id.startsWith("o1") || 
                id.startsWith("o3") ||
                id.startsWith("o4")
            }
            .filterNot { id ->
                id.contains("embedding") ||
                id.contains("whisper") ||
                id.contains("tts") ||
                id.contains("dall-e") ||
                id.contains("realtime") ||
                id.contains("transcription") ||
                id.contains("audio")
            }
        
        assertTrue(chatModels.contains("gpt-4o"))
        assertTrue(chatModels.contains("gpt-4o-mini"))
        assertTrue(chatModels.contains("o1"))
        assertTrue(chatModels.contains("o3"))
        assertFalse(chatModels.contains("text-embedding-ada-002"))
        assertFalse(chatModels.contains("whisper-1"))
        assertFalse(chatModels.contains("tts-1"))
        assertFalse(chatModels.contains("dall-e-3"))
        assertFalse(chatModels.contains("gpt-4o-realtime-preview"))
    }

    @Test
    fun `Anthropic models should be filtered to Claude only`() {
        val allModels = listOf(
            "claude-sonnet-4-5-20250929",
            "claude-haiku-4-5-20251001",
            "claude-opus-4-1-20250805",
            "some-other-model"
        )
        
        val claudeModels = allModels.filter { it.startsWith("claude-") }
        
        assertEquals(3, claudeModels.size)
        assertTrue(claudeModels.all { it.startsWith("claude-") })
    }

    @Test
    fun `Anthropic models should be sorted by tier`() {
        val models = listOf(
            "claude-haiku-4-5-20251001",
            "claude-sonnet-4-5-20250929",
            "claude-opus-4-1-20250805"
        )
        
        val sorted = models.sortedByDescending { id ->
            when {
                id.contains("opus") -> 3
                id.contains("sonnet") -> 2
                id.contains("haiku") -> 1
                else -> 0
            }
        }
        
        assertTrue(sorted[0].contains("opus"))
        assertTrue(sorted[1].contains("sonnet"))
        assertTrue(sorted[2].contains("haiku"))
    }

    @Test
    fun `Gemini models should be filtered correctly`() {
        val allModels = listOf(
            "gemini-2.5-pro",
            "gemini-2.5-flash",
            "gemini-2.0-flash",
            "gemini-2.5-flash-image",
            "models/gemini-2.5-pro",
            "text-bison"
        )
        
        val geminiModels = allModels
            .map { it.removePrefix("models/") }
            .filter { it.startsWith("gemini-") }
        
        assertTrue(geminiModels.contains("gemini-2.5-pro"))
        assertTrue(geminiModels.contains("gemini-2.5-flash"))
        assertTrue(geminiModels.contains("gemini-2.5-flash-image"))
        assertFalse(geminiModels.contains("text-bison"))
    }

    @Test
    fun `Gemini models should be sorted by generation and tier`() {
        val models = listOf(
            "gemini-2.0-flash",
            "gemini-2.5-flash",
            "gemini-2.5-pro",
            "gemini-3-flash",
            "gemini-3-pro"
        )
        
        val sorted = models.sortedByDescending { id ->
            when {
                id.contains("3") && id.contains("pro") -> 6
                id.contains("3") && id.contains("flash") -> 5
                id.contains("2.5") && id.contains("pro") -> 4
                id.contains("2.5") && id.contains("flash") -> 3
                id.contains("2.0") -> 2
                else -> 1
            }
        }
        
        assertEquals("gemini-3-pro", sorted[0])
        assertEquals("gemini-3-flash", sorted[1])
        assertEquals("gemini-2.5-pro", sorted[2])
        assertEquals("gemini-2.5-flash", sorted[3])
        assertEquals("gemini-2.0-flash", sorted[4])
    }

    @Test
    fun `OpenAI models should be sorted by family priority`() {
        val models = listOf(
            "gpt-3.5-turbo",
            "gpt-4o",
            "o1",
            "o3",
            "gpt-5"
        )
        
        val sorted = models.sortedByDescending { id ->
            when {
                id.startsWith("gpt-5") -> 5
                id.startsWith("o3") || id.startsWith("o4") -> 4
                id.startsWith("gpt-4") -> 3
                id.startsWith("o1") -> 2
                else -> 1
            }
        }
        
        assertEquals("gpt-5", sorted[0])
        assertEquals("o3", sorted[1])
        assertEquals("gpt-4o", sorted[2])
        assertEquals("o1", sorted[3])
        assertEquals("gpt-3.5-turbo", sorted[4])
    }
}
