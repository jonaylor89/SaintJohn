package com.jonaylor.saintjohn.data.repository

import com.jonaylor.saintjohn.data.local.dao.WeatherDao
import com.jonaylor.saintjohn.data.local.entity.WeatherEntity
import com.jonaylor.saintjohn.data.remote.WeatherApi
import com.jonaylor.saintjohn.domain.model.WeatherData
import com.jonaylor.saintjohn.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi,
    private val weatherDao: WeatherDao
) : WeatherRepository {

    override fun getCurrentWeather(): Flow<WeatherData?> {
        return weatherDao.getCurrentWeather().map { entity ->
            entity?.toDomainModel()
        }
    }

    override suspend fun refreshWeather(location: String?) {
        try {
            val city = location ?: "London" // Default location
            val response = weatherApi.getCurrentWeather(
                city = city,
                apiKey = WeatherApi.API_KEY
            )

            val entity = WeatherEntity(
                location = response.cityName,
                temp = response.main.temp.roundToInt(),
                feelsLike = response.main.feelsLike.roundToInt(),
                condition = response.weather.firstOrNull()?.main ?: "Unknown",
                description = response.weather.firstOrNull()?.description ?: "",
                tempMin = response.main.tempMin.roundToInt(),
                tempMax = response.main.tempMax.roundToInt(),
                humidity = response.main.humidity,
                pressure = response.main.pressure,
                iconCode = response.weather.firstOrNull()?.icon ?: "",
                timestamp = System.currentTimeMillis()
            )

            weatherDao.insertWeather(entity)

            // Clean up old weather data (older than 1 hour)
            val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
            weatherDao.deleteOldWeather(oneHourAgo)
        } catch (e: Exception) {
            e.printStackTrace()
            // Keep cached data on error
        }
    }

    override suspend fun refreshWeatherByCoords(latitude: Double, longitude: Double) {
        try {
            val response = weatherApi.getCurrentWeatherByCoords(
                latitude = latitude,
                longitude = longitude,
                apiKey = WeatherApi.API_KEY
            )

            val entity = WeatherEntity(
                location = response.cityName,
                temp = response.main.temp.roundToInt(),
                feelsLike = response.main.feelsLike.roundToInt(),
                condition = response.weather.firstOrNull()?.main ?: "Unknown",
                description = response.weather.firstOrNull()?.description ?: "",
                tempMin = response.main.tempMin.roundToInt(),
                tempMax = response.main.tempMax.roundToInt(),
                humidity = response.main.humidity,
                pressure = response.main.pressure,
                iconCode = response.weather.firstOrNull()?.icon ?: "",
                timestamp = System.currentTimeMillis()
            )

            weatherDao.insertWeather(entity)

            // Clean up old weather data (older than 1 hour)
            val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
            weatherDao.deleteOldWeather(oneHourAgo)
        } catch (e: Exception) {
            e.printStackTrace()
            // Keep cached data on error
        }
    }

    private fun WeatherEntity.toDomainModel(): WeatherData {
        return WeatherData(
            temp = temp,
            feelsLike = feelsLike,
            condition = condition,
            description = description,
            highLow = Pair(tempMax, tempMin),
            humidity = humidity,
            pressure = pressure,
            location = location,
            iconCode = iconCode,
            timestamp = timestamp
        )
    }
}
