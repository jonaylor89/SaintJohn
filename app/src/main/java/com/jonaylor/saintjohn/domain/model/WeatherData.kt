package com.jonaylor.saintjohn.domain.model

data class WeatherData(
    val temp: Int,
    val condition: String,
    val highLow: Pair<Int, Int>,
    val location: String = ""
)
