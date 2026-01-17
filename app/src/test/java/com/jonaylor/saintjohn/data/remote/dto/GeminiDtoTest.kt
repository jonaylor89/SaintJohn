package com.jonaylor.saintjohn.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for Gemini DTO parsing, including image responses.
 */
class GeminiDtoTest {

    private val gson = Gson()

    @Test
    fun `GeminiPart should parse text content`() {
        val json = """{"text": "Hello world"}"""
        val part = gson.fromJson(json, GeminiPart::class.java)
        
        assertEquals("Hello world", part.text)
        assertNull(part.inlineData)
    }

    @Test
    fun `GeminiPart should parse inline image data`() {
        val json = """{
            "inlineData": {
                "mimeType": "image/png",
                "data": "iVBORw0KGgoAAAANS"
            }
        }"""
        val part = gson.fromJson(json, GeminiPart::class.java)
        
        assertNull(part.text)
        assertNotNull(part.inlineData)
        assertEquals("image/png", part.inlineData?.mimeType)
        assertEquals("iVBORw0KGgoAAAANS", part.inlineData?.data)
    }

    @Test
    fun `GeminiPart should parse mixed text and image`() {
        val json = """{
            "text": "Here is your image",
            "inlineData": {
                "mimeType": "image/jpeg",
                "data": "base64imagedata"
            }
        }"""
        val part = gson.fromJson(json, GeminiPart::class.java)
        
        assertEquals("Here is your image", part.text)
        assertNotNull(part.inlineData)
        assertEquals("image/jpeg", part.inlineData?.mimeType)
    }

    @Test
    fun `GeminiInlineData should have correct mimeType formats`() {
        val pngData = GeminiInlineData(mimeType = "image/png", data = "base64")
        val jpegData = GeminiInlineData(mimeType = "image/jpeg", data = "base64")
        val webpData = GeminiInlineData(mimeType = "image/webp", data = "base64")
        
        assertTrue(pngData.mimeType.startsWith("image/"))
        assertTrue(jpegData.mimeType.startsWith("image/"))
        assertTrue(webpData.mimeType.startsWith("image/"))
    }

    @Test
    fun `GeminiStreamChunk should parse response with image`() {
        val json = """{
            "candidates": [{
                "content": {
                    "parts": [
                        {"text": "Here is the image"},
                        {"inlineData": {"mimeType": "image/png", "data": "base64data"}}
                    ],
                    "role": "model"
                },
                "finishReason": "STOP"
            }]
        }"""
        val chunk = gson.fromJson(json, GeminiStreamChunk::class.java)
        
        assertNotNull(chunk.candidates)
        assertEquals(1, chunk.candidates?.size)
        
        val parts = chunk.candidates?.firstOrNull()?.content?.parts
        assertEquals(2, parts?.size)
        assertEquals("Here is the image", parts?.get(0)?.text)
        assertNotNull(parts?.get(1)?.inlineData)
        assertEquals("image/png", parts?.get(1)?.inlineData?.mimeType)
    }

    @Test
    fun `GeminiContent should handle multiple image parts`() {
        val content = GeminiContent(
            parts = listOf(
                GeminiPart(text = "Generated images:"),
                GeminiPart(inlineData = GeminiInlineData("image/png", "data1")),
                GeminiPart(inlineData = GeminiInlineData("image/jpeg", "data2"))
            ),
            role = "model"
        )
        
        assertEquals(3, content.parts.size)
        assertNotNull(content.parts[0].text)
        assertNotNull(content.parts[1].inlineData)
        assertNotNull(content.parts[2].inlineData)
    }
}
