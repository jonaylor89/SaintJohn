package com.jonaylor.saintjohn

import android.Manifest
import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.jonaylor.saintjohn.data.local.PreferencesManager
import com.jonaylor.saintjohn.domain.model.AppInfo
import com.jonaylor.saintjohn.domain.repository.AppRepository
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

    @Inject
    lateinit var appRepository: AppRepository

    private var packageChangeReceiver: BroadcastReceiver? = null

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        registerPackageChangeReceiver()

        android.util.Log.d("SaintJohn", "LAUNCHING AGENTIC VERSION 1.3")
        android.widget.Toast.makeText(this, "Agentic Saint John v1.3", android.widget.Toast.LENGTH_LONG).show()

        setContent {
            val onboardingCompleted by preferencesManager.onboardingCompleted.collectAsState(initial = false)

            // Check if we need to show onboarding
            LaunchedEffect(onboardingCompleted) {
                if (!onboardingCompleted) {
                    // Navigate to onboarding
                    startActivity(Intent(this@MainActivity, com.jonaylor.saintjohn.presentation.onboarding.OnboardingActivity::class.java))
                    finish()
                    return@LaunchedEffect
                }
            }

            // Only show main content if onboarding is completed
            if (onboardingCompleted) {
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

                // Request location permissions
                val locationPermissionsState = rememberMultiplePermissionsState(
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )

                // Request calendar permission
                val calendarPermissionState = rememberMultiplePermissionsState(
                    listOf(Manifest.permission.READ_CALENDAR)
                )

                LaunchedEffect(Unit) {
                    // Request location permissions for weather
                    if (!locationPermissionsState.allPermissionsGranted) {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }

                    // Request calendar permission
                    if (!calendarPermissionState.allPermissionsGranted) {
                        calendarPermissionState.launchMultiplePermissionRequest()
                    }

                    // Request usage stats permission on first launch
                    if (!hasUsageStatsPermission()) {
                        requestUsageStatsPermission()
                    }
                }

                var selectedApp by remember { mutableStateOf<AppInfo?>(null) }

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
                            onAppLongClick = { app -> selectedApp = app }
                        )
                        2 -> HomeScreen() // Right: Home/Settings
                    }
                }

                // App options dialog
                selectedApp?.let { app ->
                    AppOptionsDialog(
                        app = app,
                        onDismiss = { selectedApp = null },
                        onAppInfo = {
                            openAppInfo(app.packageName)
                            selectedApp = null
                        },
                        onUninstall = {
                            uninstallApp(app.packageName)
                            selectedApp = null
                        }
                    )
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

    private fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    private fun registerPackageChangeReceiver() {
        packageChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action ?: return
                val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)

                when (action) {
                    Intent.ACTION_PACKAGE_ADDED,
                    Intent.ACTION_PACKAGE_REMOVED -> {
                        if (!isReplacing) {
                            lifecycleScope.launch {
                                appRepository.refreshApps()
                            }
                        }
                    }
                    Intent.ACTION_PACKAGE_CHANGED,
                    Intent.ACTION_PACKAGE_REPLACED -> {
                        lifecycleScope.launch {
                            appRepository.refreshApps()
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }

        ContextCompat.registerReceiver(
            this,
            packageChangeReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        packageChangeReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                // Already unregistered
            }
        }
        packageChangeReceiver = null
    }
}

@Composable
fun AppOptionsDialog(
    app: AppInfo,
    onDismiss: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App name
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Options
                DialogOption(
                    icon = Icons.Default.Info,
                    text = "App Info",
                    onClick = onAppInfo
                )

                Spacer(modifier = Modifier.height(12.dp))

                DialogOption(
                    icon = Icons.Default.Delete,
                    text = "Uninstall",
                    onClick = onUninstall
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogOption(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}