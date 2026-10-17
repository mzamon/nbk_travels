package com.example.nbk_travels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.example.nbk_travels.ui.NbkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NbkTheme {
                Text(text = "Welcome to NBK Travels")
            }
        }
    }
}
