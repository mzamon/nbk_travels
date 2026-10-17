package com.example.nbk_travels.data

import android.content.Context

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("nbk_prefs", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
