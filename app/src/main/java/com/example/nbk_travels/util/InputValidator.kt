package com.example.nbk_travels.util

import java.time.LocalDate
import java.time.format.DateTimeParseException

object InputValidator {
    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateName(name: String): String? = when {
        name.trim().length < 2 -> "Enter your full name (at least 2 characters)."
        name.trim().length > 100 -> "Your name is too long."
        else -> null
    }

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Enter your email address."
        !emailPattern.matches(email.trim()) -> "Enter a valid email address."
        email.trim().length > 100 -> "Email address is too long."
        else -> null
    }

    fun validatePhone(phone: String): String? {
        val cleaned = phone.filter { it.isDigit() || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')' }
        val digits = cleaned.count { it.isDigit() }
        return when {
            phone.isBlank() -> "Enter your phone number."
            cleaned != phone -> "Use only digits and common phone-number symbols."
            digits !in 7..15 -> "Enter a valid phone number."
            else -> null
        }
    }

    fun validatePassword(password: String): String? = when {
        password.length < 8 -> "Password must be at least 8 characters."
        password.length > 128 -> "Password is too long."
        else -> null
    }

    fun validateConfirmPassword(password: String, confirm: String): String? =
        if (password != confirm) "Passwords do not match." else null

    fun validateBooking(
        pickup: String,
        dropoff: String,
        date: String,
        time: String,
        passengers: Int,
        fare: String
    ): String? {
        if (pickup.trim().length < 2) return "Enter a pickup location."
        if (dropoff.trim().length < 2) return "Enter a destination."
        if (pickup.trim().equals(dropoff.trim(), ignoreCase = true)) return "Pickup and destination must be different."

        val travelDate = try {
            LocalDate.parse(date)
        } catch (_: DateTimeParseException) {
            return "Choose a valid travel date."
        }
        if (travelDate.isBefore(LocalDate.now())) return "Travel date cannot be in the past."

        if (!Regex("^([01]\\d|2[0-3]):[0-5]\\d$").matches(time)) return "Choose a valid travel time."
        if (passengers !in 1..15) return "Passenger count must be between 1 and 15."
        val parsedFare = fare.toDoubleOrNull() ?: return "Enter a valid fare in South African rand."
        if (parsedFare < 0.0 || !parsedFare.isFinite()) return "Fare cannot be negative."
        return null
    }
}
