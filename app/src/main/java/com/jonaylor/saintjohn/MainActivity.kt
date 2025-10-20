package com.jonaylor.saintjohn

import android.app.AppOpsManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.jonaylor.saintjohn.data.local.PreferencesManager
import com.jonaylor.saintjohn.domain.model.AppInfo
import com.jonaylor.saintjohn.presentation.drawer.DrawerScreen
import com.jonaylor.saintjohn.presentation.home.HomeScreen
import com.jonaylor.saintjohn.presentation.root.RootScreen
import com.jonaylor.saintjohn.util.theme.LauncherTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState(initial = "BLLOC")
            val coroutineScope = rememberCoroutineScope()

            LauncherTheme(themeMode = themeMode) {
                // Configure system bars appearance
                val darkTheme = when (themeMode) {
                    "BLLOC" -> true
                    "SUN" -> false
                    "COLOR" -> isSystemInDarkTheme()
                    else -> true
                }

                SideEffect {
                    val window = this@MainActivity.window
                    window.statusBarColor = Color.Transparent.toArgb()
                    window.navigationBarColor = Color.Transparent.toArgb()
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }

                // Request usage stats permission on first launch
                LaunchedEffect(Unit) {
                    if (!hasUsageStatsPermission()) {
                        requestUsageStatsPermission()
                    }
                }

                val pagerState = rememberPagerState(
                    initialPage = 1, // Start at center (Drawer screen)
                    pageCount = { 3 }
                )

                // Handle back button - go to middle page (drawer)
                BackHandler {
                    if (pagerState.currentPage != 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                    // If already on drawer page, do nothing (don't close launcher)
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> RootScreen() // Left: Cards
                        1 -> DrawerScreen( // Center: App Drawer
                            onAppClick = { app -> launchApp(app) },
                            onAppLongClick = { app -> showAppDashboard(app) }
                        )
                        2 -> HomeScreen() // Right: Home/Settings
                    }
                }
            }
        }
    }

    private fun launchApp(app: AppInfo) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showAppDashboard(app: AppInfo) {
        // TODO: Implement app dashboard bottom sheet
    }

    private fun hasUsageStatsPermission(): Boolean {
        return try {
            val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    private fun requestUsageStatsPermission() {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}