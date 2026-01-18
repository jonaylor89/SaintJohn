package com.jonaylor.saintjohn.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AnthropicRequest(
    val model: String,
    val messages: List<AnthropicMessage>,
    @SerializedName("max_tokens")
    val maxTokens: Int = 4096,
    val stream: Boolean = false,
    val system: String? = null,
    val tools: List<AnthropicTool>? = null
)

data class AnthropicTool(
    val name: String,
    val description: String,
    @SerializedName("input_schema")
    val inputSchema: AnthropicSchema
)

data class AnthropicSchema(
    val type: String = "object",
    val properties: Map<String, AnthropicSchemaProperty>,
    val required: List<String>? = null
)

data class AnthropicSchemaProperty(
    val type: String,
    val description: String? = null,
    val enum: List<String>? = null
)

data class AnthropicMessage(
    val role: String,
    val content: Any // Can be String or List<AnthropicContent>
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
    val text: String? = null,
    val id: String? = null,
    val name: String? = null,
    val input: Map<String, Any?>? = null,
    @SerializedName("tool_use_id")
    val toolUseId: String? = null,
    val content: String? = null
)

data class AnthropicStreamChunk(
    val type: String,
    val index: Int? = null,
    val delta: AnthropicDelta? = null,
    @SerializedName("content_block")
    val contentBlock: AnthropicContentBlock? = null
)

data class AnthropicContentBlock(
    val type: String,
    val text: String? = null,
    val id: String? = null,
    val name: String? = null,
    val input: Map<String, Any?>? = null
)

data class AnthropicDelta(
    val type: String,
    val text: String? = null,
    val thinking: String? = null,
    @SerializedName("partial_json")
    val partialJson: String? = null
)

// Models list response
data class AnthropicModelsResponse(
    val data: List<AnthropicModel>,
    @SerializedName("has_more")
    val hasMore: Boolean = false
)

data class AnthropicModel(
    val id: String,
    @SerializedName("display_name")
    val displayName: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    val type: String = "model"
)
