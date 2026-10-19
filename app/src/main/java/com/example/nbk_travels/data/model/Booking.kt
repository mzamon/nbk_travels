package com.example.nbk_travels.data.model

data class Booking(
    val id: String = "",
    val userId: String = "",
    val pickup: String = "",
    val dropoff: String = "",
    val date: String = "",
    val passengers: Int = 1,
    val fare: Double = 0.0,
    val status: String = "Pending",
    val createdAt: Long = 0L
)
