package com.jonaylor.saintjohn.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AnthropicRequest(
    val model: String,
    val messages: List<AnthropicMessage>,
    @SerializedName("max_tokens")
    val maxTokens: Int = 4096,
    val stream: Boolean = false
)

data class AnthropicMessage(
    val role: String,
    val content: String
)

data class AnthropicResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<AnthropicContent>,
    val model: String,
    @SerializedName("stop_reason")
    val stopReason: String? = null
)

data class AnthropicContent(
    val type: String,
    val text: String
)

data class AnthropicStreamChunk(
    val type: String,
    val index: Int? = null,
    val delta: AnthropicDelta? = null,
    @SerializedName("content_block")
    val contentBlock: AnthropicContent? = null
)

data class AnthropicDelta(
    val type: String,
    val text: String? = null
)
