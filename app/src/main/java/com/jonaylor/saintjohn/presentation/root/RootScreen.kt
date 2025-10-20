package com.jonaylor.saintjohn.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jonaylor.saintjohn.presentation.root.components.CalendarCard
import com.jonaylor.saintjohn.presentation.root.components.NotesCard
import com.jonaylor.saintjohn.presentation.root.components.WeatherCard

@Composable
fun RootScreen(
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    notesViewModel: NotesViewModel = hiltViewModel()
) {
    val weatherUiState by weatherViewModel.uiState.collectAsState()
    val calendarUiState by calendarViewModel.uiState.collectAsState()
    val notesUiState by notesViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Weather Card
        WeatherCard(
            weatherData = weatherUiState.weatherData,
            isLoading = weatherUiState.isLoading,
            onRefresh = { weatherViewModel.refreshWeather() },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Calendar Card
        CalendarCard(
            events = calendarUiState.events,
            isLoading = calendarUiState.isLoading,
            onRefresh = { calendarViewModel.refreshEvents() },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Notes Card
        NotesCard(
            notes = notesUiState.notes,
            isLoading = notesUiState.isLoading,
            onAddNote = { content -> notesViewModel.addNote(content) },
            onUpdateNote = { note -> notesViewModel.updateNote(note) },
            onDeleteNote = { note -> notesViewModel.deleteNote(note) },
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
