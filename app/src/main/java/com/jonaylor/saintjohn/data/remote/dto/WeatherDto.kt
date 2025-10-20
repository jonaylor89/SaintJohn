package com.jonaylor.saintjohn.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * OpenWeatherMap API response models
 */
data class WeatherResponse(
    @SerializedName("weather")
    val weather: List<WeatherCondition>,
    @SerializedName("main")
    val main: MainWeather,
    @SerializedName("name")
    val cityName: String,
    @SerializedName("dt")
    val timestamp: Long
)

data class WeatherCondition(
    @SerializedName("id")
    val id: Int,
    @SerializedName("main")
    val main: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("icon")
    val icon: String
)

data class MainWeather(
    @SerializedName("temp")
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("temp_min")
    val tempMin: Double,
    @SerializedName("temp_max")
    val tempMax: Double,
    @SerializedName("pressure")
    val pressure: Int,
    @SerializedName("humidity")
    val humidity: Int
)
