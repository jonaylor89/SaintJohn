package com.jonaylor.saintjohn.domain.repository

import com.jonaylor.saintjohn.domain.model.AppCategory
import com.jonaylor.saintjohn.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getAllApps(): Flow<List<AppInfo>>
    fun getAppsByCategory(category: AppCategory): Flow<List<AppInfo>>
    suspend fun updateAppCategory(packageName: String, category: AppCategory)
    suspend fun updateAppVisibility(packageName: String, isHidden: Boolean)
    suspend fun updateAppLockStatus(packageName: String, isLocked: Boolean)
    suspend fun updateAppColorMode(packageName: String, forceColor: Boolean)
    suspend fun refreshApps()
}
