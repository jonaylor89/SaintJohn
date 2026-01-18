package com.jonaylor.saintjohn.domain.repository

interface AppLauncher {
    fun launchApp(packageName: String): Boolean
    fun launchAppByName(appName: String): Boolean
}
