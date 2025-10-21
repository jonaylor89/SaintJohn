package com.jonaylor.saintjohn.data.remote

import com.jonaylor.saintjohn.data.remote.dto.OpenAIModelsResponse
import com.jonaylor.saintjohn.data.remote.dto.OpenAIRequest
import com.jonaylor.saintjohn.data.remote.dto.OpenAIResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

interface OpenAIApi {

    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: OpenAIRequest
    ): OpenAIResponse

    @Streaming
    @POST("chat/completions")
    suspend fun createChatCompletionStream(
        @Header("Authorization") authorization: String,
        @Body request: OpenAIRequest
    ): ResponseBody

    @GET("models")
    suspend fun listModels(
        @Header("Authorization") authorization: String
    ): OpenAIModelsResponse

    companion object {
        const val BASE_URL = "https://api.openai.com/v1/"
        const val DEFAULT_MODEL = "gpt-4o-mini" // Cost-effective model
    }
}
