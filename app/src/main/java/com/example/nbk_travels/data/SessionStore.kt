package com.example.nbk_travels.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Stores profile preferences locally and encrypts the API bearer token with an Android Keystore key. */
class SessionStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("nbk_app_session", Context.MODE_PRIVATE)

    fun currentUser(): UserProfile? {
        val raw = prefs.getString(KEY_USER, null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            UserProfile(
                id = json.optString("id"),
                fullName = json.optString("fullName"),
                email = json.optString("email"),
                phone = json.optString("phone"),
                status = json.optString("status", "verified"),
                notificationsEnabled = json.optBoolean("notificationsEnabled", true),
                darkMode = json.optBoolean("darkMode", false),
                language = json.optString("language", "English")
            )
        }.getOrNull()
    }

    fun saveSession(user: UserProfile, token: String? = null) {
        val json = JSONObject().apply {
            put("id", user.id)
            put("fullName", user.fullName)
            put("email", user.email)
            put("phone", user.phone)
            put("status", user.status)
            put("notificationsEnabled", user.notificationsEnabled)
            put("darkMode", user.darkMode)
            put("language", user.language)
        }
        val editor = prefs.edit().putString(KEY_USER, json.toString())
        if (token.isNullOrBlank()) {
            editor.remove(KEY_TOKEN_ENCRYPTED).remove(KEY_TOKEN_LEGACY)
        } else {
            val encrypted = runCatching { encrypt(token) }.getOrNull()
            if (encrypted != null) {
                editor.putString(KEY_TOKEN_ENCRYPTED, encrypted).remove(KEY_TOKEN_LEGACY)
            } else {
                // Fail closed: do not persist the bearer token if Android Keystore encryption fails.
                editor.remove(KEY_TOKEN_ENCRYPTED).remove(KEY_TOKEN_LEGACY)
            }
        }
        editor.apply()
    }

    fun accessToken(): String? {
        val encrypted = prefs.getString(KEY_TOKEN_ENCRYPTED, null)
        if (encrypted != null) return runCatching { decrypt(encrypted) }.getOrNull()
        // Remove any plaintext token left by an older prototype build.
        if (prefs.contains(KEY_TOKEN_LEGACY)) prefs.edit().remove(KEY_TOKEN_LEGACY).apply()
        return null
    }

    fun clearSession() {
        prefs.edit().remove(KEY_USER).remove(KEY_TOKEN_ENCRYPTED).remove(KEY_TOKEN_LEGACY).apply()
    }

    fun updateSessionUser(user: UserProfile) = saveSession(user, accessToken())

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP or Base64.URL_SAFE)
        val data = Base64.encodeToString(cipherText, Base64.NO_WRAP or Base64.URL_SAFE)
        return "$iv:$data"
    }

    private fun decrypt(encoded: String): String {
        val parts = encoded.split(':', limit = 2)
        require(parts.size == 2) { "Invalid encrypted session token." }
        val iv = Base64.decode(parts[0], Base64.NO_WRAP or Base64.URL_SAFE)
        val data = Base64.decode(parts[1], Base64.NO_WRAP or Base64.URL_SAFE)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(data), Charsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    companion object {
        private const val KEY_ALIAS = "nbk_session_token_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_USER = "current_user"
        private const val KEY_TOKEN_ENCRYPTED = "api_access_token_enc"
        private const val KEY_TOKEN_LEGACY = "api_access_token"
    }
}
