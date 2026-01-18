package com.jonaylor.saintjohn.domain.agent.skills

import com.jonaylor.saintjohn.domain.agent.AgentSkill
import com.jonaylor.saintjohn.domain.agent.SkillParameter
import com.jonaylor.saintjohn.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetWeatherSkill @Inject constructor(
    private val weatherRepository: WeatherRepository
) : AgentSkill(
    name = "get_weather",
    description = "Gets the current weather information for a location or the current location.",
    params = listOf(
        SkillParameter(
            name = "location",
            type = "string",
            description = "The city name to get weather for (optional, defaults to current location)",
            required = false
        )
    )
) {
    override suspend fun execute(args: Map<String, Any?>): String {
        val location = args["location"] as? String
        
        // Refresh weather if a location is provided
        if (location != null) {
            weatherRepository.refreshWeather(location)
        } else {
            weatherRepository.refreshWeather(null)
        }
        
        val weatherData = weatherRepository.getCurrentWeather().first()
        return if (weatherData != null) {
            "Weather in ${weatherData.location}: ${weatherData.temp}°C, ${weatherData.condition} (${weatherData.description}). Humidity: ${weatherData.humidity}%, Pressure: ${weatherData.pressure}hPa."
        } else {
            "Failed to retrieve weather data."
        }
    }
}
