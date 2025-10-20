package com.jonaylor.saintjohn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey
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
