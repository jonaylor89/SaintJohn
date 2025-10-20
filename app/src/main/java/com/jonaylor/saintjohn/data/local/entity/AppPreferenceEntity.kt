package com.jonaylor.saintjohn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_preferences")
data class AppPreferenceEntity(
    @PrimaryKey
    val packageName: String,
    val category: String,
    val isHidden: Boolean = false,
    val isLocked: Boolean = false,
    val isPinned: Boolean = false,
    val forceColor: Boolean = false
)
