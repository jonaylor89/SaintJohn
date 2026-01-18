package com.jonaylor.saintjohn.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val tools: List<GeminiTool>? = null,
    @SerializedName("tool_config")
    val toolConfig: GeminiToolConfig? = null,
    @SerializedName("system_instruction")
    val systemInstruction: GeminiContent? = null,
    @SerializedName("generation_config")
    val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiTool(
    @SerializedName("function_declarations")
    val functionDeclarations: List<GeminiFunctionDeclaration>? = null
)

data class GeminiToolConfig(
    @SerializedName("function_calling_config")
    val functionCallingConfig: GeminiFunctionCallingConfig? = null
)

data class GeminiFunctionCallingConfig(
    val mode: String = "AUTO", // AUTO, ANY, NONE
    @SerializedName("allowed_function_names")
    val allowedFunctionNames: List<String>? = null
)

data class GeminiFunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: GeminiSchema? = null
)

data class GeminiSchema(
    val type: String = "OBJECT",
    val properties: Map<String, GeminiSchemaProperty>? = null,
    val required: List<String>? = null
)

data class GeminiSchemaProperty(
    val type: String, // STRING, INTEGER, BOOLEAN, NUMBER
    val description: String? = null,
    val enum: List<String>? = null
)

data class GeminiGenerationConfig(
    @SerializedName("response_modalities")
    val responseModalities: List<String>? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null,
    @SerializedName("functionCall")
    val functionCall: GeminiFunctionCall? = null,
    @SerializedName("functionResponse")
    val functionResponse: GeminiFunctionResponse? = null
)

data class GeminiFunctionCall(
    val name: String,
    val args: Map<String, Any?>
)

data class GeminiFunctionResponse(
    val name: String,
    val response: Map<String, Any?>
)

data class GeminiInlineData(
    val mimeType: String,
    val data: String // base64-encoded image data
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

// Models list response
data class GeminiModelsResponse(
    val models: List<GeminiModel>
)

data class GeminiModel(
    val name: String,
    val version: String? = null,
    val displayName: String? = null,
    val description: String? = null,
    val inputTokenLimit: Int? = null,
    val outputTokenLimit: Int? = null,
    val supportedGenerationMethods: List<String>? = null
)
