package com.jonaylor.saintjohn.domain.repository

import com.jonaylor.saintjohn.domain.model.WeatherData
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getCurrentWeather(): Flow<WeatherData?>
    suspend fun refreshWeather(location: String? = null)
    suspend fun refreshWeatherByCoords(latitude: Double, longitude: Double)
}
