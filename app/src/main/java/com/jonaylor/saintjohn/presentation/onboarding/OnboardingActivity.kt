package com.jonaylor.saintjohn.presentation.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.jonaylor.saintjohn.MainActivity
import com.jonaylor.saintjohn.data.local.PreferencesManager
import com.jonaylor.saintjohn.util.theme.LauncherTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled in composable */ }

    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled in composable */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (shouldSkipOnboarding()) {
            lifecycleScope.launch {
                preferencesManager.setOnboardingCompleted(true)
                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                finish()
            }
            return
        }

        setContent {
            LauncherTheme {
                OnboardingScreen(
                    onComplete = {
                        lifecycleScope.launch {
                            preferencesManager.setOnboardingCompleted(true)
                            startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                            finish()
                        }
                    },
                    onRequestLocationPermission = {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    onRequestCalendarPermission = {
                        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                    },
                    onRequestUsageStatsPermission = {
                        try {
                            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        } catch (e: Exception) {
                            // Fallback
                        }
                    },
                    onSetDefaultLauncher = {
                        try {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                Intent(Settings.ACTION_HOME_SETTINGS)
                            } else {
                                Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_HOME)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            }
                            startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Intent.ACTION_MAIN)
                                intent.addCategory(Intent.CATEGORY_HOME)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            } catch (e2: Exception) {
                                e2.printStackTrace()
                            }
                        }
                    },
                    checkLocationPermission = {
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    },
                    checkCalendarPermission = {
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_CALENDAR
                        ) == PackageManager.PERMISSION_GRANTED
                    },
                    checkUsageStatsPermission = { hasUsageStatsPermission() },
                    checkIsDefaultLauncher = { isDefaultLauncher() }
                )
            }
        }
    }

    private fun shouldSkipOnboarding(): Boolean {
        val hasLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCalendar = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        val hasUsageStats = hasUsageStatsPermission()

        return hasLocation && hasCalendar && hasUsageStats && isDefaultLauncher()
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName == packageName
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    onRequestUsageStatsPermission: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    checkLocationPermission: () -> Boolean,
    checkCalendarPermission: () -> Boolean,
    checkUsageStatsPermission: () -> Boolean,
    checkIsDefaultLauncher: () -> Boolean
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()

    var locationGranted by remember { mutableStateOf(checkLocationPermission()) }
    var calendarGranted by remember { mutableStateOf(checkCalendarPermission()) }
    var usageStatsGranted by remember { mutableStateOf(checkUsageStatsPermission()) }
    var isDefaultLauncher by remember { mutableStateOf(checkIsDefaultLauncher()) }

    LaunchedEffect(pagerState.currentPage) {
        locationGranted = checkLocationPermission()
        calendarGranted = checkCalendarPermission()
        usageStatsGranted = checkUsageStatsPermission()
        isDefaultLauncher = checkIsDefaultLauncher()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> WelcomePage(
                    onNext = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
                1 -> DefaultLauncherPage(
                    isGranted = isDefaultLauncher,
                    onSetDefault = onSetDefaultLauncher,
                    onNext = {
                        scope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    },
                    onSkip = {
                        scope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    }
                )
                2 -> PermissionPage(
                    title = "Weather at a Glance",
                    icon = "📍",
                    description = "Get live weather updates on your home screen based on your location.",
                    isGranted = locationGranted,
                    onGrant = {
                        onRequestLocationPermission()
                        scope.launch {
                            kotlinx.coroutines.delay(500)
                            locationGranted = checkLocationPermission()
                        }
                    },
                    onNext = {
                        scope.launch {
                            pagerState.animateScrollToPage(3)
                        }
                    },
                    onSkip = {
                        scope.launch {
                            pagerState.animateScrollToPage(3)
                        }
                    }
                )
                3 -> PermissionPage(
                    title = "Your Schedule",
                    icon = "📅",
                    description = "See your upcoming calendar events without opening any apps.",
                    isGranted = calendarGranted,
                    onGrant = {
                        onRequestCalendarPermission()
                        scope.launch {
                            kotlinx.coroutines.delay(500)
                            calendarGranted = checkCalendarPermission()
                        }
                    },
                    onNext = {
                        scope.launch {
                            pagerState.animateScrollToPage(4)
                        }
                    },
                    onSkip = {
                        scope.launch {
                            pagerState.animateScrollToPage(4)
                        }
                    }
                )
                4 -> PermissionPage(
                    title = "Smart App Sorting",
                    icon = "📊",
                    description = "Let Saint John learn which apps you use most to show them first.",
                    isGranted = usageStatsGranted,
                    onGrant = onRequestUsageStatsPermission,
                    onNext = onComplete,
                    onSkip = onComplete,
                    isLastPage = true
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (pagerState.currentPage == index) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun WelcomePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Saint John",
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "A minimal launcher with AI conversations, smart widgets, and organized apps.",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Get Started", fontSize = 18.sp)
        }
    }
}

@Composable
fun DefaultLauncherPage(
    isGranted: Boolean,
    onSetDefault: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏠",
            fontSize = 64.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Make Saint John Your Default Launcher",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You can change this anytime in system settings.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (isGranted) {
            Text(
                text = "✓ Saint John is your default launcher",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Continue", fontSize = 18.sp)
            }
        } else {
            Button(
                onClick = onSetDefault,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Set as Default", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for Now", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun PermissionPage(
    title: String,
    icon: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    isLastPage: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 64.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (isGranted) {
            Text(
                text = "✓ Permission granted",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (isLastPage) "Get Started" else "Continue", fontSize = 18.sp)
            }
        } else {
            Button(
                onClick = onGrant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Enable", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLastPage) "Skip & Start" else "Skip", fontSize = 16.sp)
            }
        }
    }
}
