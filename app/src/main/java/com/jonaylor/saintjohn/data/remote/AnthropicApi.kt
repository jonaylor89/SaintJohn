package com.jonaylor.saintjohn.data.remote

import com.jonaylor.saintjohn.data.remote.dto.AnthropicRequest
import com.jonaylor.saintjohn.data.remote.dto.AnthropicResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

interface AnthropicApi {

    @POST("v1/messages")
    suspend fun createMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: AnthropicRequest
    ): AnthropicResponse

    @Streaming
    @POST("v1/messages")
    suspend fun createMessageStream(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: AnthropicRequest
    ): ResponseBody

    companion object {
        const val BASE_URL = "https://api.anthropic.com/"
        const val DEFAULT_MODEL = "claude-sonnet-4-5-20250929"
    }
}
