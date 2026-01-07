package com.jonaylor.saintjohn.presentation.home.components

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for markdown parsing logic used in chat bubbles.
 * 
 * The markdown parser should:
 * - Extract code blocks with language tags
 * - Handle multiple code blocks
 * - Handle code blocks without language tags
 * - Preserve text before and after code blocks
 */
class MarkdownParsingTest {

    @Test
    fun `should extract single code block with language`() {
        val markdown = """
            Here is some code:
            ```kotlin
            fun hello() {
                println("Hello")
            }
            ```
            That was the code.
        """.trimIndent()

        val blocks = extractCodeBlocks(markdown)

        assertEquals(1, blocks.size)
        assertEquals("kotlin", blocks[0].language)
        assertTrue(blocks[0].code.contains("fun hello()"))
    }

    @Test
    fun `should extract code block without language`() {
        val markdown = """
            ```
            some code here
            ```
        """.trimIndent()

        val blocks = extractCodeBlocks(markdown)

        assertEquals(1, blocks.size)
        assertNull(blocks[0].language)
        assertEquals("some code here", blocks[0].code)
    }

    @Test
    fun `should extract multiple code blocks`() {
        val markdown = """
            First block:
            ```python
            print("hello")
            ```
            Second block:
            ```javascript
            console.log("world")
            ```
        """.trimIndent()

        val blocks = extractCodeBlocks(markdown)

        assertEquals(2, blocks.size)
        assertEquals("python", blocks[0].language)
        assertEquals("javascript", blocks[1].language)
    }

    @Test
    fun `should return empty list for markdown without code blocks`() {
        val markdown = "This is just regular **bold** and *italic* text."

        val blocks = extractCodeBlocks(markdown)

        assertTrue(blocks.isEmpty())
    }

    @Test
    fun `should handle empty code block`() {
        val markdown = """
            ```
            ```
        """.trimIndent()

        val blocks = extractCodeBlocks(markdown)

        assertEquals(1, blocks.size)
        assertEquals("", blocks[0].code)
    }

    @Test
    fun `should preserve code block indices`() {
        val markdown = "Before ```kotlin\ncode\n``` After"

        val blocks = extractCodeBlocks(markdown)

        assertEquals(1, blocks.size)
        assertTrue(blocks[0].startIndex > 0)
        assertTrue(blocks[0].endIndex < markdown.length)
    }

    @Test
    fun `should handle various programming languages`() {
        val languages = listOf("java", "swift", "rust", "go", "cpp", "sql", "bash", "json")

        for (lang in languages) {
            val markdown = "```$lang\ncode\n```"
            val blocks = extractCodeBlocks(markdown)

            assertEquals("Should extract $lang block", 1, blocks.size)
            assertEquals(lang, blocks[0].language)
        }
    }

    private data class CodeBlockInfo(
        val code: String,
        val language: String?,
        val startIndex: Int,
        val endIndex: Int
    )

    private fun extractCodeBlocks(markdown: String): List<CodeBlockInfo> {
        val blocks = mutableListOf<CodeBlockInfo>()
        val pattern = Regex("```(\\w*)\\n([\\s\\S]*?)```")

        pattern.findAll(markdown).forEach { match ->
            val language = match.groupValues[1].takeIf { it.isNotEmpty() }
            val code = match.groupValues[2].trimEnd()
            blocks.add(
                CodeBlockInfo(
                    code = code,
                    language = language,
                    startIndex = match.range.first,
                    endIndex = match.range.last + 1
                )
            )
        }

        return blocks
    }
}
