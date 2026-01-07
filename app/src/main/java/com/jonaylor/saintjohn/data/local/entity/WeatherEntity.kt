package com.jonaylor.saintjohn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey
    val id: Int = 1, // Single row with ID 1 for current weather
    val location: String,
    val temp: Int,
    val feelsLike: Int,
    val condition: String,
    val description: String,
    val tempMin: Int,
    val tempMax: Int,
    val humidity: Int,
    val pressure: Int,
    val iconCode: String,
    val timestamp: Long
)
