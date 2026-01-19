package com.jonaylor.saintjohn.data.remote.dto

data class TavilySearchRequest(
    val query: String,
    val search_depth: String = "basic",
    val max_results: Int = 5,
    val include_answer: Boolean = false
)

data class TavilySearchResponse(
    val query: String,
    val results: List<TavilyResult>,
    val response_time: Double? = null
)

data class TavilyResult(
    val url: String,
    val title: String,
    val content: String,
    val score: Double? = null
)
