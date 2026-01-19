package com.jonaylor.saintjohn.data.remote

import com.jonaylor.saintjohn.data.remote.dto.TavilySearchRequest
import com.jonaylor.saintjohn.data.remote.dto.TavilySearchResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface TavilyApi {
    companion object {
        const val BASE_URL = "https://api.tavily.com/"
    }

    @POST("search")
    suspend fun search(
        @Header("Authorization") authHeader: String,
        @Body request: TavilySearchRequest
    ): TavilySearchResponse
}
