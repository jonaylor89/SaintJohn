package com.jonaylor.saintjohn.domain.agent.skills

import com.google.gson.Gson
import com.jonaylor.saintjohn.domain.agent.AgentSkill
import com.jonaylor.saintjohn.domain.agent.SkillParameter
import com.jonaylor.saintjohn.domain.repository.WebSearchRepository
import javax.inject.Inject

class WebSearchSkill @Inject constructor(
    private val webSearchRepository: WebSearchRepository
) : AgentSkill(
    name = "web_search",
    description = "Search the web for current information, news, documentation, or any topic. Use this when the user asks about recent events, needs up-to-date information, or when you need to look something up. Returns search results with titles, URLs, and content snippets.",
    params = listOf(
        SkillParameter(
            name = "query",
            type = "string",
            description = "The search query to look up on the web",
            required = true
        ),
        SkillParameter(
            name = "max_results",
            type = "integer",
            description = "Maximum number of results to return (1-10, default 5)",
            required = false
        )
    )
) {
    override suspend fun execute(args: Map<String, Any?>): String {
        val query = args["query"] as? String
            ?: return """{"error": "Missing required parameter: query"}"""
        
        val maxResults = (args["max_results"] as? Number)?.toInt() ?: 5

        val result = webSearchRepository.search(query, maxResults)

        return result.fold(
            onSuccess = { sources ->
                val resultsForLLM = sources.map { source ->
                    mapOf(
                        "index" to source.index,
                        "url" to source.url,
                        "title" to (source.title ?: ""),
                        "content" to (source.snippet ?: "")
                    )
                }
                Gson().toJson(mapOf("results" to resultsForLLM))
            },
            onFailure = { error ->
                Gson().toJson(mapOf("error" to error.message))
            }
        )
    }
}
