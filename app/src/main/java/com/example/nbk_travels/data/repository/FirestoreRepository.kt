package com.example.nbk_travels.data.repository

import com.example.nbk_travels.data.model.Booking
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun saveUser(userId: String, fullName: String, email: String): Task<Void> =
        firestore.collection("users").document(userId).set(
            mapOf("fullName" to fullName.trim(), "email" to email.trim())
        )

    fun saveBooking(booking: Booking): Task<Void> {
        val reference = if (booking.id.isBlank()) {
            firestore.collection("bookings").document()
        } else {
            firestore.collection("bookings").document(booking.id)
        }
        return reference.set(booking.copy(id = reference.id))
    }

    fun bookingsForUser(userId: String): Query =
        firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
}
