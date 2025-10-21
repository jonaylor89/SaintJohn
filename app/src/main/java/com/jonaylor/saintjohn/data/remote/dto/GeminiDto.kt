package com.jonaylor.saintjohn.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

data class GeminiPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>
)

data class GeminiCandidate(
    val content: GeminiContent,
    @SerializedName("finishReason")
    val finishReason: String? = null
)

data class GeminiStreamChunk(
    val candidates: List<GeminiCandidate>? = null
)
