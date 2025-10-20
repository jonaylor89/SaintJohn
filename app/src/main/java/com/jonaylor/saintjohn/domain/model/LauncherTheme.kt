package com.jonaylor.saintjohn.domain.model

sealed class LauncherTheme {
    object BllocMode : LauncherTheme() // Dark monochrome
    object SunMode : LauncherTheme() // Light monochrome
    object NativeColor : LauncherTheme() // Full color
}
