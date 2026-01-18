package com.jonaylor.saintjohn.data.remote

import com.jonaylor.saintjohn.data.remote.dto.AnthropicModelsResponse
import com.jonaylor.saintjohn.data.remote.dto.AnthropicRequest
import com.jonaylor.saintjohn.data.remote.dto.AnthropicResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

interface AnthropicApi {

    @POST("https://api.anthropic.com/v1/messages")
    suspend fun createMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: AnthropicRequest
    ): AnthropicResponse

    @Streaming
    @POST("https://api.anthropic.com/v1/messages")
    suspend fun createMessageStream(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: AnthropicRequest
    ): ResponseBody

    @GET("v1/models")
    suspend fun listModels(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01"
    ): AnthropicModelsResponse

    companion object {
        const val BASE_URL = "https://api.anthropic.com/"
        const val DEFAULT_MODEL = "claude-3-5-sonnet-20241022"
    }
}
