package com.example.nbk_travels.data.model

data class WeatherResponse(
    val date: String,
    val highCelsius: Double,
    val lowCelsius: Double,
    val rainProbability: Int
)
