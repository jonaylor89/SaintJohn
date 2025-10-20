package com.jonaylor.saintjohn.domain.usecase

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAppUsageStatsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    /**
     * Get usage time for a specific app over the last 24 hours
     * Returns usage time in milliseconds
     */
    fun getAppUsageTime(packageName: String): Long {
        if (!hasUsageStatsPermission()) {
            return 0L
        }

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startTime = calendar.timeInMillis

        val usageStats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        return usageStats?.find { it.packageName == packageName }?.totalTimeInForeground ?: 0L
    }

    /**
     * Get usage stats for all apps over the last 24 hours
     * Returns a map of package names to usage time in milliseconds
     */
    fun getAllAppUsageStats(): Map<String, Long> {
        if (!hasUsageStatsPermission()) {
            return emptyMap()
        }

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startTime = calendar.timeInMillis

        val usageStats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return emptyMap()

        return usageStats.associate { stats ->
            stats.packageName to stats.totalTimeInForeground
        }
    }

    /**
     * Check if the app has usage stats permission
     */
    fun hasUsageStatsPermission(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }
}
