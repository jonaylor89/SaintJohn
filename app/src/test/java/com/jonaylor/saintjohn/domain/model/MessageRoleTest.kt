package com.jonaylor.saintjohn.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for message role handling in chat.
 */
class MessageRoleTest {

    @Test
    fun `USER role should be recognized`() {
        val role = MessageRole.valueOf("USER")
        assertEquals(MessageRole.USER, role)
    }

    @Test
    fun `ASSISTANT role should be recognized`() {
        val role = MessageRole.valueOf("ASSISTANT")
        assertEquals(MessageRole.ASSISTANT, role)
    }

    @Test
    fun `SYSTEM role should be recognized`() {
        val role = MessageRole.valueOf("SYSTEM")
        assertEquals(MessageRole.SYSTEM, role)
    }

    @Test
    fun `all roles should be present`() {
        val roles = MessageRole.entries
        assertEquals(3, roles.size)
        assertTrue(roles.contains(MessageRole.USER))
        assertTrue(roles.contains(MessageRole.ASSISTANT))
        assertTrue(roles.contains(MessageRole.SYSTEM))
    }
}
