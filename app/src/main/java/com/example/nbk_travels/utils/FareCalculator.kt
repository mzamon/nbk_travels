package com.example.nbk_travels.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

object FareCalculator {
    private const val EARTH_RADIUS_KM = 6_371.0
    private const val ROAD_FACTOR = 1.3
    private const val BASE_FARE = 150.0
    private const val RATE_PER_ROAD_KM = 14.0
    private const val PASSENGER_SURCHARGE = 25.0
    private const val MINIMUM_FARE = 250.0

    fun haversineDistanceKm(
        latitude1: Double,
        longitude1: Double,
        latitude2: Double,
        longitude2: Double
    ): Double {
        val latitudeDelta = Math.toRadians(latitude2 - latitude1)
        val longitudeDelta = Math.toRadians(longitude2 - longitude1)
        val a = sin(latitudeDelta / 2).let { it * it } +
            cos(Math.toRadians(latitude1)) *
            cos(Math.toRadians(latitude2)) *
            sin(longitudeDelta / 2).let { it * it }
        return EARTH_RADIUS_KM * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun calculateFare(straightLineDistanceKm: Double, passengers: Int): Double {
        require(straightLineDistanceKm >= 0) { "Distance cannot be negative." }
        require(passengers in 1..15) { "Passengers must be between 1 and 15." }
        val roadDistance = straightLineDistanceKm * ROAD_FACTOR
        val calculated = maxOf(
            MINIMUM_FARE,
            BASE_FARE + roadDistance * RATE_PER_ROAD_KM + passengers * PASSENGER_SURCHARGE
        )
        return round(calculated / 5.0) * 5.0
    }
}
