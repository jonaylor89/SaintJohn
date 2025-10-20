package com.jonaylor.saintjohn.domain.model

data class WeatherData(
    val temp: Int,
    val feelsLike: Int,
    val condition: String,
    val description: String,
    val highLow: Pair<Int, Int>,
    val humidity: Int,
    val pressure: Int,
    val location: String = "",
    val iconCode: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
