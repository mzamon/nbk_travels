package com.example.nbk_travels.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BookingListScreen() {
    Text(text = "Booking List Screen")
}

@Composable
fun BookingDetailScreen(bookingId: String) {
    Text(text = "Booking Detail for $bookingId")
}
