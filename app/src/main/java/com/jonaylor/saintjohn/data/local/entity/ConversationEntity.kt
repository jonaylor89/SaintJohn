package com.jonaylor.saintjohn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val provider: String,
    val model: String = "",
    val createdAt: Long,
    val updatedAt: Long
)
