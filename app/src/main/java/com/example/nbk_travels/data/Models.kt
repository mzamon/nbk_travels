package com.example.nbk_travels.data

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val status: String = "verified",
    val notificationsEnabled: Boolean = true,
    val darkMode: Boolean = false,
    val language: String = "English"
)

data class StoredUser(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val status: String
)

data class Booking(
    val id: String,
    val ownerEmail: String,
    val pickup: String,
    val dropoff: String,
    val date: String,
    val time: String,
    val passengers: Int,
    val fare: Double,
    val status: String = "pending",
    val driverName: String? = null,
    val vehicleName: String? = null
)

data class BookingDraft(
    val pickup: String,
    val dropoff: String,
    val date: String,
    val time: String,
    val passengers: Int,
    val fare: Double
)

data class OperationResult(
    val success: Boolean,
    val message: String,
    val user: UserProfile? = null,
    val token: String? = null,
    val booking: Booking? = null
)
