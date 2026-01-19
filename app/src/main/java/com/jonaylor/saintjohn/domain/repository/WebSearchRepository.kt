package com.jonaylor.saintjohn.domain.repository

import com.jonaylor.saintjohn.domain.model.MessageSource

interface WebSearchRepository {
    suspend fun search(query: String, maxResults: Int = 5): Result<List<MessageSource>>
}
