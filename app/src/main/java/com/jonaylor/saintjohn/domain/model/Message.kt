package com.jonaylor.saintjohn.domain.model

data class Message(
    val id: Long = 0,
    val conversationId: Long = 0,
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}
