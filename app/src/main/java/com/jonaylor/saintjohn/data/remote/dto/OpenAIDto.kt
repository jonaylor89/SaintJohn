package com.jonaylor.saintjohn.data.remote.dto

import com.google.gson.annotations.SerializedName

// Request models
data class OpenAIRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<OpenAIMessage>,
    @SerializedName("stream")
    val stream: Boolean = false,
    @SerializedName("tools")
    val tools: List<OpenAITool>? = null,
    @SerializedName("tool_choice")
    val toolChoice: Any? = null // "auto", "none", "required" or specific tool
)

data class OpenAIMessage(
    @SerializedName("role")
    val role: String, // "user", "assistant", "system", "tool"
    @SerializedName("content")
    val content: String?,
    @SerializedName("tool_calls")
    val toolCalls: List<OpenAIToolCall>? = null,
    @SerializedName("tool_call_id")
    val toolCallId: String? = null // Required for tool messages
)

data class OpenAITool(
    @SerializedName("type")
    val type: String = "function",
    @SerializedName("function")
    val function: OpenAIFunction
)

data class OpenAIFunction(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("parameters")
    val parameters: OpenAISchema
)

data class OpenAISchema(
    @SerializedName("type")
    val type: String = "object",
    @SerializedName("properties")
    val properties: Map<String, OpenAISchemaProperty>,
    @SerializedName("required")
    val required: List<String>? = null
)

data class OpenAISchemaProperty(
    @SerializedName("type")
    val type: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("enum")
    val enum: List<String>? = null
)

data class OpenAIToolCall(
    @SerializedName("id")
    val id: String,
    @SerializedName("type")
    val type: String = "function",
    @SerializedName("function")
    val function: OpenAIFunctionCall
)

data class OpenAIFunctionCall(
    @SerializedName("name")
    val name: String,
    @SerializedName("arguments")
    val arguments: String
)

// Response models
data class OpenAIResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("object")
    val objectType: String,
    @SerializedName("created")
    val created: Long,
    @SerializedName("model")
    val model: String,
    @SerializedName("choices")
    val choices: List<OpenAIChoice>,
    @SerializedName("usage")
    val usage: OpenAIUsage?
)

// Streaming response models
data class OpenAIStreamChunk(
    @SerializedName("id")
    val id: String,
    @SerializedName("object")
    val objectType: String,
    @SerializedName("created")
    val created: Long,
    @SerializedName("model")
    val model: String,
    @SerializedName("choices")
    val choices: List<OpenAIStreamChoice>
)

data class OpenAIStreamChoice(
    @SerializedName("index")
    val index: Int,
    @SerializedName("delta")
    val delta: OpenAIDelta,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class OpenAIDelta(
    @SerializedName("role")
    val role: String?,
    @SerializedName("content")
    val content: String?,
    @SerializedName("reasoning_content")
    val reasoningContent: String? = null,
    @SerializedName("tool_calls")
    val toolCalls: List<OpenAIToolCallChunk>? = null
)

data class OpenAIToolCallChunk(
    @SerializedName("index")
    val index: Int,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("function")
    val function: OpenAIFunctionCallChunk? = null
)

data class OpenAIFunctionCallChunk(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("arguments")
    val arguments: String? = null
)

data class OpenAIChoice(
    @SerializedName("index")
    val index: Int,
    @SerializedName("message")
    val message: OpenAIMessage,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class OpenAIUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

// Error response
data class OpenAIError(
    @SerializedName("error")
    val error: OpenAIErrorDetails
)

data class OpenAIErrorDetails(
    @SerializedName("message")
    val message: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("code")
    val code: String?
)

// Models list response
data class OpenAIModelsResponse(
    @SerializedName("object")
    val objectType: String,
    @SerializedName("data")
    val data: List<OpenAIModel>
)

data class OpenAIModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("object")
    val objectType: String,
    @SerializedName("created")
    val created: Long,
    @SerializedName("owned_by")
    val ownedBy: String
)
