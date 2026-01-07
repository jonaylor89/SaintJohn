package com.jonaylor.saintjohn.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for LLM provider configuration.
 */
class LLMProviderTest {

    @Test
    fun `OPENAI provider should be available`() {
        val provider = LLMProvider.valueOf("OPENAI")
        assertEquals(LLMProvider.OPENAI, provider)
    }

    @Test
    fun `ANTHROPIC provider should be available`() {
        val provider = LLMProvider.valueOf("ANTHROPIC")
        assertEquals(LLMProvider.ANTHROPIC, provider)
    }

    @Test
    fun `GOOGLE provider should be available`() {
        val provider = LLMProvider.valueOf("GOOGLE")
        assertEquals(LLMProvider.GOOGLE, provider)
    }

    @Test
    fun `all providers should have display names`() {
        LLMProvider.entries.forEach { provider ->
            assertNotNull("Display name should not be null", provider.displayName)
            assertTrue("Display name should not be empty", provider.displayName.isNotBlank())
        }
    }

    @Test
    fun `provider count should be 3`() {
        assertEquals(3, LLMProvider.entries.size)
    }

    @Test
    fun `invalid provider name should throw exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            LLMProvider.valueOf("INVALID_PROVIDER")
        }
    }
}
