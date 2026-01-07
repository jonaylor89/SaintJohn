package com.jonaylor.saintjohn.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonaylor.saintjohn.domain.model.WeatherData
import com.jonaylor.saintjohn.domain.repository.WeatherRepository
import com.jonaylor.saintjohn.domain.usecase.GetLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherUiState(
    val weatherData: WeatherData? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val getLocationUseCase: GetLocationUseCase
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val uiState: StateFlow<WeatherUiState> = weatherRepository.getCurrentWeather()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        ).let { weatherFlow ->
            MutableStateFlow(WeatherUiState()).also { state ->
                viewModelScope.launch {
                    weatherFlow.collect { weather ->
                        state.value = WeatherUiState(
                            weatherData = weather,
                            isLoading = false,
                            error = if (weather == null) "Unable to load weather" else null
                        )
                    }
                }
            }
        }

    init {
        // Load weather on startup
        refreshWeather()
    }

    fun refreshWeather(location: String? = null, forceAccurateLocation: Boolean = true) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Force high accuracy when user explicitly refreshes to get fresh location
                // Use balanced accuracy for automatic background refreshes
                val locationData = getLocationUseCase.getCurrentLocation(forceHighAccuracy = forceAccurateLocation)
                if (locationData != null) {
                    weatherRepository.refreshWeatherByCoords(
                        latitude = locationData.latitude,
                        longitude = locationData.longitude
                    )
                } else if (location != null) {
                    // Only use city name if explicitly provided
                    weatherRepository.refreshWeather(location)
                }
                // If no location available, keep existing weather data
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshWeatherByCoords(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                weatherRepository.refreshWeatherByCoords(latitude, longitude)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
