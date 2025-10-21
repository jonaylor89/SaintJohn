package com.jonaylor.saintjohn.domain.model

data class Note(
    val id: Long = 0,
    val title: String = "",
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
