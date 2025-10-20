package com.jonaylor.saintjohn.domain.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable? = null,
    val category: AppCategory = AppCategory.OTHER,
    val isSystemApp: Boolean = false,
    val isHidden: Boolean = false,
    val isLocked: Boolean = false,
    val isPinned: Boolean = false,
    val forceColor: Boolean = false,
    val usageTime: Long = 0L, // milliseconds
    val lastUsed: Long = 0L // timestamp
)
