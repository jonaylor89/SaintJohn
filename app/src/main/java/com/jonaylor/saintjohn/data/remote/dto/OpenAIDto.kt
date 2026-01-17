package com.jonaylor.saintjohn.data.remote.dto

import com.google.gson.annotations.SerializedName

// Request models
data class OpenAIRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<OpenAIMessage>,
    @SerializedName("stream")
    val stream: Boolean = false
)

data class OpenAIMessage(
    @SerializedName("role")
    val role: String, // "user", "assistant", "system"
    @SerializedName("content")
    val content: String
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
    val reasoningContent: String? = null
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
