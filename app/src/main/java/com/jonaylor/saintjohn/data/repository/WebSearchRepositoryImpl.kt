package com.jonaylor.saintjohn.data.repository

import com.jonaylor.saintjohn.data.local.PreferencesManager
import com.jonaylor.saintjohn.data.remote.TavilyApi
import com.jonaylor.saintjohn.data.remote.dto.TavilySearchRequest
import com.jonaylor.saintjohn.domain.model.MessageSource
import com.jonaylor.saintjohn.domain.repository.WebSearchRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSearchRepositoryImpl @Inject constructor(
    private val tavilyApi: TavilyApi,
    private val preferencesManager: PreferencesManager
) : WebSearchRepository {

    override suspend fun search(query: String, maxResults: Int): Result<List<MessageSource>> {
        return try {
            val apiKey = preferencesManager.tavilyApiKey.first()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Tavily API key not configured. Add it in Settings."))
            }

            val request = TavilySearchRequest(
                query = query,
                max_results = maxResults.coerceIn(1, 10)
            )

            val response = tavilyApi.search(
                authHeader = "Bearer $apiKey",
                request = request
            )

            val sources = response.results.mapIndexed { index, result ->
                MessageSource(
                    index = index + 1,
                    url = result.url,
                    title = result.title,
                    snippet = result.content.take(300)
                )
            }

            Result.success(sources)
        } catch (e: Exception) {
            Result.failure(Exception("Web search failed: ${e.message}"))
        }
    }
}
