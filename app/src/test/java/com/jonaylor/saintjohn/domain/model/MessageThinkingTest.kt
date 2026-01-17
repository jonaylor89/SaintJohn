package com.jonaylor.saintjohn.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for thinking/reasoning content in messages.
 */
class MessageThinkingTest {

    @Test
    fun `Message should have null thinking by default`() {
        val message = Message(
            content = "Hello",
            role = MessageRole.ASSISTANT
        )
        
        assertNull(message.thinking)
        assertNull(message.thinkingSummary)
    }

    @Test
    fun `Message should store thinking content`() {
        val thinkingContent = "Let me think about this step by step..."
        val message = Message(
            content = "The answer is 42",
            role = MessageRole.ASSISTANT,
            thinking = thinkingContent
        )
        
        assertEquals(thinkingContent, message.thinking)
    }

    @Test
    fun `Message should store thinking summary`() {
        val message = Message(
            content = "Result",
            role = MessageRole.ASSISTANT,
            thinking = "Long detailed reasoning...",
            thinkingSummary = "Quick summary"
        )
        
        assertEquals("Long detailed reasoning...", message.thinking)
        assertEquals("Quick summary", message.thinkingSummary)
    }

    @Test
    fun `Message with only thinking and no content should be valid for streaming`() {
        val message = Message(
            content = "",
            role = MessageRole.ASSISTANT,
            thinking = "I'm thinking about this..."
        )
        
        assertTrue(message.content.isEmpty())
        assertNotNull(message.thinking)
        assertTrue(message.thinking!!.isNotEmpty())
    }

    @Test
    fun `isNullOrEmpty should work correctly for thinking`() {
        val messageWithThinking = Message(
            content = "Answer",
            role = MessageRole.ASSISTANT,
            thinking = "Reasoning"
        )
        
        val messageWithoutThinking = Message(
            content = "Answer",
            role = MessageRole.ASSISTANT
        )
        
        val messageWithEmptyThinking = Message(
            content = "Answer",
            role = MessageRole.ASSISTANT,
            thinking = ""
        )
        
        assertFalse(messageWithThinking.thinking.isNullOrEmpty())
        assertTrue(messageWithoutThinking.thinking.isNullOrEmpty())
        assertTrue(messageWithEmptyThinking.thinking.isNullOrEmpty())
    }
}
