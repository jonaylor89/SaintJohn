package com.jonaylor.saintjohn.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonaylor.saintjohn.domain.model.CalendarEvent
import com.jonaylor.saintjohn.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalendarUiState(
    val events: List<CalendarEvent> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val uiState: StateFlow<CalendarUiState> = calendarRepository.getUpcomingEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ).let { eventsFlow ->
            MutableStateFlow(CalendarUiState()).also { state ->
                viewModelScope.launch {
                    eventsFlow.collect { events ->
                        state.value = CalendarUiState(
                            events = events,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            }
        }

    init {
        // Load events on startup
        refreshEvents()
    }

    fun refreshEvents() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                calendarRepository.refreshEvents()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
