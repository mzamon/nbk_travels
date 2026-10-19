package com.example.nbk_travels.data.remote

import com.example.nbk_travels.data.model.GeocodingResponse
import com.example.nbk_travels.data.model.WeatherResponse
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class OpenMeteoApi {
    fun geocodeTown(town: String): GeocodingResponse {
        val encodedTown = URLEncoder.encode(town.trim(), Charsets.UTF_8.name())
        val geocoding = getJson(
            "https://geocoding-api.open-meteo.com/v1/search?name=$encodedTown&count=1&language=en&format=json"
        )
        val result = geocoding.optJSONArray("results")?.optJSONObject(0)
            ?: throw IllegalArgumentException("Unknown town: $town")
        return GeocodingResponse(
            latitude = result.getDouble("latitude"),
            longitude = result.getDouble("longitude"),
            name = result.optString("name", town.trim())
        )
    }

    fun weatherForTown(town: String, date: String): WeatherResponse {
        val location = geocodeTown(town)
        val forecast = getJson(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=${location.latitude}&longitude=${location.longitude}" +
                "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
                "&timezone=auto"
        )
        val dates = forecast.getJSONObject("daily").getJSONArray("time")
        val index = (0 until dates.length()).firstOrNull { dates.getString(it) == date }
            ?: throw IllegalArgumentException("Weather is unavailable for $date.")
        val daily = forecast.getJSONObject("daily")
        return WeatherResponse(
            date = date,
            highCelsius = daily.getJSONArray("temperature_2m_max").getDouble(index),
            lowCelsius = daily.getJSONArray("temperature_2m_min").getDouble(index),
            rainProbability = daily.getJSONArray("precipitation_probability_max").optInt(index, 0)
        )
    }

    private fun getJson(url: String): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/json")
        }
        return try {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("Weather service returned HTTP ${connection.responseCode}.")
            }
            JSONObject(response)
        } finally {
            connection.disconnect()
        }
    }
}
