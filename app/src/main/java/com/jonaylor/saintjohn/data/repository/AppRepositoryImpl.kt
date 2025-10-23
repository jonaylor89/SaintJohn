package com.jonaylor.saintjohn.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.jonaylor.saintjohn.data.local.dao.AppPreferenceDao
import com.jonaylor.saintjohn.data.local.entity.AppPreferenceEntity
import com.jonaylor.saintjohn.domain.model.AppCategory
import com.jonaylor.saintjohn.domain.model.AppInfo
import com.jonaylor.saintjohn.domain.repository.AppRepository
import com.jonaylor.saintjohn.domain.usecase.AppCategorizationUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferenceDao: AppPreferenceDao,
    private val categorizationUseCase: AppCategorizationUseCase,
    private val getAppUsageStatsUseCase: com.jonaylor.saintjohn.domain.usecase.GetAppUsageStatsUseCase
) : AppRepository {

    private val refreshTrigger = MutableStateFlow(0L)

    override fun getAllApps(): Flow<List<AppInfo>> {
        return combine(
            appPreferenceDao.getAllPreferences(),
            refreshTrigger
        ) { preferences, _ ->
            preferences
        }.map { preferences ->
                val installedApps = withContext(Dispatchers.IO) {
                    loadInstalledApps()
                }
                val preferenceMap = preferences.associateBy { it.packageName }
                installedApps.map { appInfo ->
                    val pref = preferenceMap[appInfo.packageName]
                    appInfo.copy(
                        category = pref?.category?.let { AppCategory.valueOf(it) } ?: appInfo.category,
                        isHidden = pref?.isHidden ?: false,
                        isLocked = pref?.isLocked ?: false,
                        isPinned = pref?.isPinned ?: false,
                        forceColor = pref?.forceColor ?: false
                    )
                }.filterNot { it.isHidden }
            }
    }

    override fun getAppsByCategory(category: AppCategory): Flow<List<AppInfo>> {
        return combine(
            appPreferenceDao.getAllPreferences(),
            refreshTrigger
        ) { preferences, _ ->
            preferences
        }.map { preferences ->
                val installedApps = withContext(Dispatchers.IO) {
                    loadInstalledApps()
                }
                val preferenceMap = preferences.associateBy { it.packageName }
                installedApps.mapNotNull { appInfo ->
                    val pref = preferenceMap[appInfo.packageName]
                    val finalCategory = pref?.category?.let { AppCategory.valueOf(it) } ?: appInfo.category
                    val isHidden = pref?.isHidden ?: false

                    if (finalCategory == category && !isHidden) {
                        appInfo.copy(
                            category = finalCategory,
                            isHidden = isHidden,
                            isLocked = pref?.isLocked ?: false,
                            isPinned = pref?.isPinned ?: false,
                            forceColor = pref?.forceColor ?: false
                        )
                    } else null
                }
            }
    }

    override suspend fun updateAppCategory(packageName: String, category: AppCategory) {
        withContext(Dispatchers.IO) {
            val existing = appPreferenceDao.getPreference(packageName)
            if (existing != null) {
                appPreferenceDao.updatePreference(existing.copy(category = category.name))
            } else {
                appPreferenceDao.insertPreference(
                    AppPreferenceEntity(
                        packageName = packageName,
                        category = category.name
                    )
                )
            }
        }
    }

    override suspend fun updateAppVisibility(packageName: String, isHidden: Boolean) {
        withContext(Dispatchers.IO) {
            val existing = appPreferenceDao.getPreference(packageName)
            if (existing != null) {
                appPreferenceDao.updatePreference(existing.copy(isHidden = isHidden))
            } else {
                appPreferenceDao.insertPreference(
                    AppPreferenceEntity(
                        packageName = packageName,
                        category = AppCategory.OTHER.name,
                        isHidden = isHidden
                    )
                )
            }
        }
    }

    override suspend fun updateAppLockStatus(packageName: String, isLocked: Boolean) {
        withContext(Dispatchers.IO) {
            val existing = appPreferenceDao.getPreference(packageName)
            if (existing != null) {
                appPreferenceDao.updatePreference(existing.copy(isLocked = isLocked))
            } else {
                appPreferenceDao.insertPreference(
                    AppPreferenceEntity(
                        packageName = packageName,
                        category = AppCategory.OTHER.name,
                        isLocked = isLocked
                    )
                )
            }
        }
    }

    override suspend fun updateAppColorMode(packageName: String, forceColor: Boolean) {
        withContext(Dispatchers.IO) {
            val existing = appPreferenceDao.getPreference(packageName)
            if (existing != null) {
                appPreferenceDao.updatePreference(existing.copy(forceColor = forceColor))
            } else {
                appPreferenceDao.insertPreference(
                    AppPreferenceEntity(
                        packageName = packageName,
                        category = AppCategory.OTHER.name,
                        forceColor = forceColor
                    )
                )
            }
        }
    }

    override suspend fun refreshApps() {
        // Trigger a reload of apps by updating the refresh trigger
        refreshTrigger.value = System.currentTimeMillis()
    }

    private fun loadInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val launcherApps = pm.queryIntentActivities(intent, 0)

        // Get all usage stats at once for better performance
        val usageStatsMap = getAppUsageStatsUseCase.getAllAppUsageStats()

        return launcherApps.mapNotNull { resolveInfo ->
            try {
                val packageName = resolveInfo.activityInfo.packageName
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val label = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                val category = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    categorizationUseCase.categorize(packageName, appInfo.category)
                } else {
                    categorizationUseCase.categorize(packageName, ApplicationInfo.CATEGORY_UNDEFINED)
                }

                val usageTime = usageStatsMap[packageName] ?: 0L

                AppInfo(
                    packageName = packageName,
                    label = label,
                    icon = icon,
                    category = category,
                    isSystemApp = isSystemApp,
                    usageTime = usageTime
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.label.lowercase() }
    }
}
