package com.jonaylor.saintjohn.domain.model

data class Message(
    val id: Long = 0,
    val conversationId: Long = 0,
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val thinking: String? = null,
    val thinkingSummary: String? = null,
    val images: List<MessageImage> = emptyList(),
    val toolCalls: List<ToolCall> = emptyList(),
    val toolResult: ToolResult? = null,
    val sources: List<MessageSource> = emptyList()
)

data class MessageImage(
    val data: String,
    val mimeType: String
)

data class ToolCall(
    val id: String,
    val name: String,
    val arguments: Map<String, Any?>
)

data class ToolResult(
    val toolCallId: String,
    val result: String,
    val isError: Boolean = false
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL
}
