package com.jonaylor.saintjohn.domain

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for system prompt handling in chat.
 */
class SystemPromptTest {

    @Test
    fun `empty system prompt should not be added to messages`() {
        val systemPrompt = ""
        val shouldInclude = systemPrompt.isNotBlank()
        
        assertFalse("Empty prompt should not be included", shouldInclude)
    }

    @Test
    fun `blank system prompt should not be added to messages`() {
        val systemPrompt = "   "
        val shouldInclude = systemPrompt.isNotBlank()
        
        assertFalse("Blank prompt should not be included", shouldInclude)
    }

    @Test
    fun `valid system prompt should be added to messages`() {
        val systemPrompt = "You are a helpful assistant."
        val shouldInclude = systemPrompt.isNotBlank()
        
        assertTrue("Valid prompt should be included", shouldInclude)
    }

    @Test
    fun `system prompt should be prepended to OpenAI messages`() {
        val systemPrompt = "Be concise."
        val userMessages = listOf(
            mapOf("role" to "user", "content" to "Hello")
        )
        
        val allMessages = buildList {
            if (systemPrompt.isNotBlank()) {
                add(mapOf("role" to "system", "content" to systemPrompt))
            }
            addAll(userMessages)
        }
        
        assertEquals(2, allMessages.size)
        assertEquals("system", allMessages[0]["role"])
        assertEquals("Be concise.", allMessages[0]["content"])
        assertEquals("user", allMessages[1]["role"])
    }

    @Test
    fun `Anthropic system field should be null for empty prompt`() {
        val systemPrompt = ""
        val anthropicSystem = systemPrompt.takeIf { it.isNotBlank() }
        
        assertNull("Anthropic system should be null for empty prompt", anthropicSystem)
    }

    @Test
    fun `Anthropic system field should be set for valid prompt`() {
        val systemPrompt = "You are a pirate."
        val anthropicSystem = systemPrompt.takeIf { it.isNotBlank() }
        
        assertEquals("You are a pirate.", anthropicSystem)
    }
}
