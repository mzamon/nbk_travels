package com.example.nbk_travels.data

import com.example.nbk_travels.BuildConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Small JSON REST client for the NBK Travel API.
 * Configure NBK_API_BASE_URL in gradle.properties to enable remote mode.
 * All calls must run off the Android main thread.
 */
class ApiClient {
    val isConfigured: Boolean
        get() = BuildConfig.API_BASE_URL.isNotBlank() && BuildConfig.API_BASE_URL.startsWith("https://")

    fun get(path: String, token: String? = null): JSONObject = request("GET", path, null, token)

    fun post(path: String, payload: JSONObject, token: String? = null): JSONObject =
        request("POST", path, payload, token)

    private fun request(method: String, path: String, payload: JSONObject?, token: String?): JSONObject {
        check(isConfigured) { "Remote API is not configured. Add NBK_API_BASE_URL to gradle.properties." }
        val base = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
        val cleanPath = path.trimStart('/')
        val connection = (URL(base + cleanPath).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/json")
            if (payload != null) setRequestProperty("Content-Type", "application/json; charset=utf-8")
            if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
            doInput = true
            if (payload != null) doOutput = true
        }
        try {
            if (payload != null) {
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(payload.toString()) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { it.readText() }
            }.orEmpty()
            if (text.isBlank()) throw IllegalStateException("The API returned an empty response (HTTP $code).")
            val json = runCatching { JSONObject(text) }.getOrElse {
                throw IllegalStateException("The API response was not valid JSON (HTTP $code).")
            }
            if (code !in 200..299 && !json.has("success")) {
                throw IllegalStateException("API request failed with HTTP $code.")
            }
            return json
        } finally {
            connection.disconnect()
        }
    }
}
