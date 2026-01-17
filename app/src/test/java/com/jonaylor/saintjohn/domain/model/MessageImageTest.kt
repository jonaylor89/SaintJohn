package com.jonaylor.saintjohn.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for message image handling in chat.
 */
class MessageImageTest {

    @Test
    fun `MessageImage should store data and mimeType`() {
        val image = MessageImage(
            data = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
            mimeType = "image/png"
        )
        
        assertNotNull(image.data)
        assertEquals("image/png", image.mimeType)
        assertTrue(image.data.isNotEmpty())
    }

    @Test
    fun `Message should contain empty images list by default`() {
        val message = Message(
            content = "Hello",
            role = MessageRole.USER
        )
        
        assertTrue(message.images.isEmpty())
    }

    @Test
    fun `Message should store images list`() {
        val images = listOf(
            MessageImage(data = "base64data1", mimeType = "image/png"),
            MessageImage(data = "base64data2", mimeType = "image/jpeg")
        )
        
        val message = Message(
            content = "Here are your images",
            role = MessageRole.ASSISTANT,
            images = images
        )
        
        assertEquals(2, message.images.size)
        assertEquals("image/png", message.images[0].mimeType)
        assertEquals("image/jpeg", message.images[1].mimeType)
    }

    @Test
    fun `Message with images and no text content should be valid`() {
        val message = Message(
            content = "",
            role = MessageRole.ASSISTANT,
            images = listOf(MessageImage(data = "base64data", mimeType = "image/png"))
        )
        
        assertTrue(message.content.isEmpty())
        assertTrue(message.images.isNotEmpty())
    }

    @Test
    fun `Message with both text and images should be valid`() {
        val message = Message(
            content = "Here is the image you requested",
            role = MessageRole.ASSISTANT,
            images = listOf(MessageImage(data = "base64data", mimeType = "image/png"))
        )
        
        assertTrue(message.content.isNotEmpty())
        assertTrue(message.images.isNotEmpty())
    }
}
