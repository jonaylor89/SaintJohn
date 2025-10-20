package com.jonaylor.saintjohn.data.local.dao

import androidx.room.*
import com.jonaylor.saintjohn.data.local.entity.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather WHERE location = :location LIMIT 1")
    fun getWeatherByLocation(location: String): Flow<WeatherEntity?>

    @Query("SELECT * FROM weather ORDER BY timestamp DESC LIMIT 1")
    fun getCurrentWeather(): Flow<WeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("DELETE FROM weather WHERE location = :location")
    suspend fun deleteWeatherByLocation(location: String)

    @Query("DELETE FROM weather WHERE timestamp < :timestamp")
    suspend fun deleteOldWeather(timestamp: Long)
}
