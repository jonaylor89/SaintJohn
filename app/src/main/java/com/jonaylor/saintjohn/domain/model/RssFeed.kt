package com.jonaylor.saintjohn.domain.model

data class RssFeed(
    val title: String,
    val url: String
)

data class RssItem(
    val title: String,
    val link: String,
    val pubDate: Long = 0L,
    val description: String? = null
)
