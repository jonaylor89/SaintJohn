package com.jonaylor.saintjohn.presentation.root

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.jonaylor.saintjohn.presentation.root.components.CalendarCard
import com.jonaylor.saintjohn.presentation.root.components.NotesCard
import com.jonaylor.saintjohn.presentation.root.components.WeatherCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    notesViewModel: NotesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val weatherUiState by weatherViewModel.uiState.collectAsState()
    val calendarUiState by calendarViewModel.uiState.collectAsState()
    val notesUiState by notesViewModel.uiState.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Refresh weather and calendar when app resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                weatherViewModel.refreshWeather()
                calendarViewModel.refreshEvents()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Periodic refresh every 30 minutes
    LaunchedEffect(Unit) {
        while (true) {
            delay(30 * 60 * 1000L) // 30 minutes
            weatherViewModel.refreshWeather()
            calendarViewModel.refreshEvents()
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            scope.launch {
                isRefreshing = true
                weatherViewModel.refreshWeather()
                calendarViewModel.refreshEvents()
                delay(1000) // Small delay to show refresh indicator
                isRefreshing = false
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
        // Weather Card
        WeatherCard(
            weatherData = weatherUiState.weatherData,
            isLoading = weatherUiState.isLoading,
            onRefresh = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                weatherViewModel.refreshWeather()
            },
            onOpenWeatherApp = {
                // Try to open Pixel Weather or any weather app
                val packageManager = context.packageManager

                // First, try to launch Pixel Weather specifically
                var weatherIntent = packageManager.getLaunchIntentForPackage("com.google.android.apps.weather")

                if (weatherIntent != null) {
                    weatherIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    try {
                        context.startActivity(weatherIntent)
                        return@WeatherCard
                    } catch (e: Exception) {
                        // Failed to launch Pixel Weather
                    }
                }

                // Try generic weather app category
                weatherIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_WEATHER)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                val weatherActivities = packageManager.queryIntentActivities(weatherIntent, 0)
                if (weatherActivities.isNotEmpty()) {
                    try {
                        context.startActivity(weatherIntent)
                        return@WeatherCard
                    } catch (e: Exception) {
                        // Failed to launch weather app
                    }
                }

                // Fallback: open in browser
                val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://www.google.com/search?q=weather")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(browserIntent)
                } catch (e: Exception) {
                    // Nothing works, ignore
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Calendar Card
        CalendarCard(
            events = calendarUiState.events,
            isLoading = calendarUiState.isLoading,
            onRefresh = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                calendarViewModel.refreshEvents()
            },
            onEventClick = { eventId ->
                // Open the event in the calendar app
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("content://com.android.calendar/events/$eventId")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // If calendar app not available, try opening in any calendar app
                    val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                        type = "vnd.android.cursor.item/event"
                        putExtra("eventId", eventId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(fallbackIntent)
                    } catch (e2: Exception) {
                        // If still fails, ignore
                    }
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

            // Notes Card
            NotesCard(
                notes = notesUiState.notes,
                isLoading = notesUiState.isLoading,
                onAddNote = { title, content -> notesViewModel.addNote(title, content) },
                onUpdateNote = { note -> notesViewModel.updateNote(note) },
                onDeleteNote = { note -> notesViewModel.deleteNote(note) },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
