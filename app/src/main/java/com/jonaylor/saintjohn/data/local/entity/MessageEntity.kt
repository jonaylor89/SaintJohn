package com.jonaylor.saintjohn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val content: String,
    val role: String, // USER, ASSISTANT, SYSTEM
    val timestamp: Long,
    val isError: Boolean = false,
    val thinking: String? = null,
    val thinkingSummary: String? = null,
    val imagesJson: String? = null, // JSON array of {data, mimeType} objects
    val toolCallsJson: String? = null, // JSON array of ToolCall objects
    val toolResultJson: String? = null // JSON object of ToolResult
)
