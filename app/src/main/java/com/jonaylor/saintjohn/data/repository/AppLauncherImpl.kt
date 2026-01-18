package com.jonaylor.saintjohn.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.jonaylor.saintjohn.domain.repository.AppLauncher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLauncherImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppLauncher {

    override fun launchApp(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun launchAppByName(appName: String): Boolean {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        // Filter for launchable apps only
        val launchableApps = packages.filter { 
            pm.getLaunchIntentForPackage(it.packageName) != null 
        }

        // 1. Try exact match (ignore case)
        var target = launchableApps.find { 
            pm.getApplicationLabel(it).toString().equals(appName, ignoreCase = true) 
        }

        // 2. Try contains match (ignore case) if no exact match
        if (target == null) {
            target = launchableApps.find { 
                pm.getApplicationLabel(it).toString().contains(appName, ignoreCase = true) 
            }
        }
        
        return target?.packageName?.let { launchApp(it) } ?: false
    }
}
