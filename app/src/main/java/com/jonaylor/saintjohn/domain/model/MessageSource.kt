package com.jonaylor.saintjohn.domain.model

data class MessageSource(
    val index: Int,
    val url: String,
    val title: String?,
    val snippet: String?
)
