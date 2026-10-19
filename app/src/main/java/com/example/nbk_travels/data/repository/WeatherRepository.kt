package com.example.nbk_travels.data.repository

import com.example.nbk_travels.data.model.WeatherResponse
import com.example.nbk_travels.data.remote.OpenMeteoApi

class WeatherRepository(private val api: OpenMeteoApi = OpenMeteoApi()) {
    fun getWeather(dropoff: String, date: String): WeatherResponse =
        api.weatherForTown(dropoff, date)
}
