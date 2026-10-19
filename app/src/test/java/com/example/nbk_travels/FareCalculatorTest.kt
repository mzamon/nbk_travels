package com.example.nbk_travels

import com.example.nbk_travels.utils.FareCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class FareCalculatorTest {
    @Test
    fun haversineDistanceIsZeroForSamePoint() {
        assertEquals(0.0, FareCalculator.haversineDistanceKm(-26.2, 28.0, -26.2, 28.0), 0.0001)
    }

    @Test
    fun fareUsesRoadFactorAndRoundsToNearestFive() {
        assertEquals(680.0, FareCalculator.calculateFare(25.0, 3), 0.0)
    }

    @Test
    fun fareHasMinimum() {
        assertEquals(250.0, FareCalculator.calculateFare(0.0, 1), 0.0)
    }
}
