package com.example.nbk_travels.data

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** App data gateway. Uses the REST API when configured; otherwise uses a persistent local prototype store. */
class AppRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("nbk_local_prototype", Context.MODE_PRIVATE)
    private val session = SessionStore(appContext)
    private val api = ApiClient()

    val remoteMode: Boolean get() = api.isConfigured

    init {
        if (!prefs.contains(KEY_USERS)) seedPrototypeData()
    }

    fun currentUser(): UserProfile? = session.currentUser()

    fun register(fullName: String, email: String, phone: String, password: String): OperationResult {
        val nameError = com.example.nbk_travels.util.InputValidator.validateName(fullName)
        if (nameError != null) return OperationResult(false, nameError)
        val emailError = com.example.nbk_travels.util.InputValidator.validateEmail(email)
        if (emailError != null) return OperationResult(false, emailError)
        val phoneError = com.example.nbk_travels.util.InputValidator.validatePhone(phone)
        if (phoneError != null) return OperationResult(false, phoneError)
        val passwordError = com.example.nbk_travels.util.InputValidator.validatePassword(password)
        if (passwordError != null) return OperationResult(false, passwordError)

        if (remoteMode) {
            val response = api.post("auth/register.php", JSONObject().apply {
                put("full_name", fullName.trim())
                put("email", email.trim().lowercase())
                put("phone", phone.trim())
                put("password", password)
            })
            return OperationResult(
                success = response.optBoolean("success", false),
                message = response.optString("message", "Registration request completed.")
            )
        }

        val normalizedEmail = email.trim().lowercase()
        val users = readUsers()
        if (users.any { it.email.equals(normalizedEmail, ignoreCase = true) }) {
            return OperationResult(false, "An account with that email already exists.")
        }
        val user = StoredUser(
            id = "local-${System.currentTimeMillis()}",
            fullName = fullName.trim(),
            email = normalizedEmail,
            phone = phone.trim(),
            passwordHash = hashPassword(password),
            status = "pending"
        )
        users.add(user)
        saveUsers(users)
        return OperationResult(true, "Registration submitted. Your account is pending administrator verification.")
    }

    fun login(email: String, password: String): OperationResult {
        val emailError = com.example.nbk_travels.util.InputValidator.validateEmail(email)
        if (emailError != null) return OperationResult(false, emailError)
        if (password.isBlank()) return OperationResult(false, "Enter your password.")

        if (remoteMode) {
            val response = api.post("auth/login.php", JSONObject().apply {
                put("email", email.trim().lowercase())
                put("password", password)
            })
            if (!response.optBoolean("success", false)) {
                return OperationResult(false, response.optString("message", "Unable to sign in."))
            }
            val userJson = response.optJSONObject("user")
                ?: return OperationResult(false, "The API did not return a user profile.")
            val user = userFromJson(userJson)
            val token = response.optString("token").takeIf { it.isNotBlank() && it != "null" }
            session.saveSession(user, token)
            return OperationResult(true, response.optString("message", "Welcome back."), user, token)
        }

        val stored = readUsers().firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
            ?: return OperationResult(false, "No account found for that email. You can create an account below.")
        if (!verifyPassword(password, stored.passwordHash)) {
            return OperationResult(false, "Incorrect password. Please try again.")
        }
        if (stored.status == "pending") {
            return OperationResult(false, "Your account is pending administrator verification.")
        }
        if (stored.status == "rejected") {
            return OperationResult(false, "Your registration was rejected. Please contact NBK Travel.")
        }
        val user = stored.toProfile()
        session.saveSession(user)
        return OperationResult(true, "Welcome back, ${user.fullName.substringBefore(' ')}.", user)
    }

    fun loginWithGoogle(idToken: String, nonce: String): OperationResult {
        if (!remoteMode) {
            return OperationResult(
                false,
                "Google sign-in was reached, but server-side token verification is not configured. Configure NBK_API_BASE_URL and the auth/google.php endpoint."
            )
        }
        val response = api.post("auth/google.php", JSONObject().apply {
            put("id_token", idToken)
            put("nonce", nonce)
        })
        if (!response.optBoolean("success", false)) {
            return OperationResult(false, response.optString("message", "Google sign-in could not be verified."))
        }
        val userJson = response.optJSONObject("user")
            ?: return OperationResult(false, "The API did not return a user profile.")
        val user = userFromJson(userJson)
        val token = response.optString("token").takeIf { it.isNotBlank() && it != "null" }
        session.saveSession(user, token)
        return OperationResult(true, response.optString("message", "Signed in with Google."), user, token)
    }

    fun requestPasswordReset(email: String): OperationResult {
        val emailError = com.example.nbk_travels.util.InputValidator.validateEmail(email)
        if (emailError != null) return OperationResult(false, emailError)
        if (!remoteMode) {
            return OperationResult(false, "Password-reset email requires the hosted API and email service to be configured.")
        }
        val response = api.post("auth/forgot-password.php", JSONObject().put("email", email.trim().lowercase()))
        return OperationResult(response.optBoolean("success", false), response.optString("message", "Request completed."))
    }

    fun listBookings(user: UserProfile): List<Booking> {
        if (remoteMode) {
            val response = api.get("bookings/list.php", session.accessToken())
            if (!response.optBoolean("success", false)) {
                throw IllegalStateException(response.optString("message", "Could not load bookings."))
            }
            val array = response.optJSONArray("bookings") ?: JSONArray()
            return (0 until array.length()).mapNotNull { index -> array.optJSONObject(index)?.let(::bookingFromJson) }
        }
        return readBookings().filter { it.ownerEmail.equals(user.email, ignoreCase = true) }
            .sortedWith(compareBy<Booking> { it.date }.thenBy { it.time })
    }

    fun createBooking(user: UserProfile, draft: BookingDraft): OperationResult {
        val validation = com.example.nbk_travels.util.InputValidator.validateBooking(
            draft.pickup, draft.dropoff, draft.date, draft.time, draft.passengers, draft.fare.toString()
        )
        if (validation != null) return OperationResult(false, validation)
        if (user.status != "verified") return OperationResult(false, "Your account must be verified before booking a shuttle.")

        if (remoteMode) {
            val response = api.post("bookings/create.php", JSONObject().apply {
                put("pickup", draft.pickup.trim())
                put("dropoff", draft.dropoff.trim())
                put("booking_date", draft.date)
                put("booking_time", draft.time)
                put("pax", draft.passengers)
                put("fare", draft.fare)
            }, session.accessToken())
            if (!response.optBoolean("success", false)) {
                return OperationResult(false, response.optString("message", "Booking could not be created."))
            }
            val booking = response.optJSONObject("booking")?.let(::bookingFromJson)
            return OperationResult(true, response.optString("message", "Booking submitted. Status: pending."), booking = booking)
        }

        val bookings = readBookings()
        val nextNumber = (bookings.mapNotNull { it.id.substringAfterLast('-').toIntOrNull() }.maxOrNull() ?: 0) + 1
        val booking = Booking(
            id = "NBK-${nextNumber.toString().padStart(4, '0')}",
            ownerEmail = user.email,
            pickup = draft.pickup.trim(),
            dropoff = draft.dropoff.trim(),
            date = draft.date,
            time = draft.time,
            passengers = draft.passengers,
            fare = draft.fare,
            status = "pending"
        )
        bookings.add(booking)
        saveBookings(bookings)
        return OperationResult(true, "Booking submitted! Status: pending.", booking = booking)
    }

    fun cancelBooking(user: UserProfile, bookingId: String): OperationResult {
        if (remoteMode) {
            val response = api.post("bookings/cancel.php", JSONObject().put("booking_id", bookingId), session.accessToken())
            return OperationResult(response.optBoolean("success", false), response.optString("message", "Cancellation request completed."))
        }
        val bookings = readBookings()
        val index = bookings.indexOfFirst { it.id == bookingId && it.ownerEmail.equals(user.email, true) }
        if (index < 0) return OperationResult(false, "Booking was not found.")
        if (bookings[index].status !in listOf("pending", "confirmed")) {
            return OperationResult(false, "This booking can no longer be cancelled.")
        }
        bookings[index] = bookings[index].copy(status = "cancelled")
        saveBookings(bookings)
        return OperationResult(true, "Booking cancelled.")
    }

    fun updateProfile(user: UserProfile, fullName: String, phone: String): OperationResult {
        val nameError = com.example.nbk_travels.util.InputValidator.validateName(fullName)
        if (nameError != null) return OperationResult(false, nameError)
        val phoneError = com.example.nbk_travels.util.InputValidator.validatePhone(phone)
        if (phoneError != null) return OperationResult(false, phoneError)
        if (remoteMode) {
            val response = api.post("profile/update.php", JSONObject().apply {
                put("full_name", fullName.trim())
                put("phone", phone.trim())
            }, session.accessToken())
            if (!response.optBoolean("success", false)) return OperationResult(false, response.optString("message", "Profile update failed."))
        } else {
            val users = readUsers().map { stored ->
                if (stored.email.equals(user.email, true)) stored.copy(fullName = fullName.trim(), phone = phone.trim()) else stored
            }
            saveUsers(users)
        }
        val updated = user.copy(fullName = fullName.trim(), phone = phone.trim())
        session.updateSessionUser(updated)
        return OperationResult(true, "Profile updated.", updated)
    }

    fun updateSettings(user: UserProfile, notificationsEnabled: Boolean, darkMode: Boolean, language: String = user.language): UserProfile {
        val updated = user.copy(
            notificationsEnabled = notificationsEnabled,
            darkMode = darkMode,
            language = language
        )
        if (remoteMode) {
            runCatching {
                api.post("profile/settings.php", JSONObject().apply {
                    put("notifications_enabled", notificationsEnabled)
                    put("dark_mode", darkMode)
                    put("language", language)
                }, session.accessToken())
            }
        }
        session.updateSessionUser(updated)
        return updated
    }

    fun changePassword(user: UserProfile, currentPassword: String, newPassword: String): OperationResult {
        val passwordError = com.example.nbk_travels.util.InputValidator.validatePassword(newPassword)
        if (passwordError != null) return OperationResult(false, passwordError)
        if (remoteMode) {
            val response = api.post("auth/change-password.php", JSONObject().apply {
                put("current_password", currentPassword)
                put("new_password", newPassword)
            }, session.accessToken())
            return OperationResult(response.optBoolean("success", false), response.optString("message", "Password update completed."))
        }
        val users = readUsers().toMutableList()
        val index = users.indexOfFirst { it.email.equals(user.email, true) }
        if (index < 0) return OperationResult(false, "Account was not found on this device.")
        if (!verifyPassword(currentPassword, users[index].passwordHash)) return OperationResult(false, "Current password is incorrect.")
        users[index] = users[index].copy(passwordHash = hashPassword(newPassword))
        saveUsers(users)
        return OperationResult(true, "Password changed successfully.")
    }

    fun logout() = session.clearSession()

    private fun seedPrototypeData() {
        val demoUser = StoredUser(
            id = "demo-user-1",
            fullName = "John Doe",
            email = "demo@nbktravels.co.za",
            phone = "082 555 0100",
            passwordHash = hashPassword("Demo123!"),
            status = "verified"
        )
        saveUsers(mutableListOf(demoUser))
        saveBookings(mutableListOf(
            Booking("NBK-0001", demoUser.email, "Sandton", "Mabopane", "2026-12-12", "15:50", 4, 6000.00, "pending"),
            Booking("NBK-0002", demoUser.email, "Airport", "Sandton", "2026-12-15", "09:30", 2, 450.00, "confirmed", "Thabo Mokoena", "Toyota Quantum"),
            Booking("NBK-0003", demoUser.email, "Pretoria", "Johannesburg", "2026-10-02", "14:00", 3, 750.00, "completed", "Lerato Nkosi", "Mercedes-Benz Sprinter")
        ))
    }

    private fun readUsers(): MutableList<StoredUser> {
        val array = runCatching { JSONArray(prefs.getString(KEY_USERS, "[]")) }.getOrElse { JSONArray() }
        return (0 until array.length()).mapNotNull { i ->
            array.optJSONObject(i)?.let { j ->
                StoredUser(j.optString("id"), j.optString("fullName"), j.optString("email"), j.optString("phone"), j.optString("passwordHash"), j.optString("status", "pending"))
            }
        }.toMutableList()
    }

    private fun saveUsers(users: List<StoredUser>) {
        val array = JSONArray()
        users.forEach { u -> array.put(JSONObject().apply {
            put("id", u.id); put("fullName", u.fullName); put("email", u.email); put("phone", u.phone)
            put("passwordHash", u.passwordHash); put("status", u.status)
        }) }
        prefs.edit().putString(KEY_USERS, array.toString()).apply()
    }

    private fun readBookings(): MutableList<Booking> {
        val array = runCatching { JSONArray(prefs.getString(KEY_BOOKINGS, "[]")) }.getOrElse { JSONArray() }
        return (0 until array.length()).mapNotNull { i -> array.optJSONObject(i)?.let(::bookingFromJson) }.toMutableList()
    }

    private fun saveBookings(bookings: List<Booking>) {
        val array = JSONArray()
        bookings.forEach { b -> array.put(JSONObject().apply {
            put("id", b.id); put("ownerEmail", b.ownerEmail); put("pickup", b.pickup); put("dropoff", b.dropoff)
            put("date", b.date); put("time", b.time); put("passengers", b.passengers); put("fare", b.fare)
            put("status", b.status); put("driverName", b.driverName); put("vehicleName", b.vehicleName)
        }) }
        prefs.edit().putString(KEY_BOOKINGS, array.toString()).apply()
    }

    private fun StoredUser.toProfile() = UserProfile(id, fullName, email, phone, status)

    private fun userFromJson(json: JSONObject) = UserProfile(
        id = json.optString("user_id", json.optString("id")),
        fullName = json.optString("full_name", json.optString("fullName", "NBK Customer")),
        email = json.optString("email"),
        phone = json.optString("phone", ""),
        status = json.optString("status", "verified"),
        notificationsEnabled = json.optBoolean("notifications_enabled", true),
        darkMode = json.optBoolean("dark_mode", false),
        language = json.optString("language", "English")
    )

    private fun bookingFromJson(json: JSONObject) = Booking(
        id = json.optString("booking_id", json.optString("id")),
        ownerEmail = json.optString("owner_email", json.optString("ownerEmail")),
        pickup = json.optString("pickup"),
        dropoff = json.optString("dropoff"),
        date = json.optString("booking_date", json.optString("date")),
        time = json.optString("booking_time", json.optString("time")),
        passengers = json.optInt("pax", json.optInt("passengers", 1)),
        fare = json.optDouble("fare", 0.0),
        status = json.optString("status", "pending"),
        driverName = json.optString("driver_name", json.optString("driverName")).takeIf { it.isNotBlank() && it != "null" },
        vehicleName = json.optString("vehicle_name", json.optString("vehicleName")).takeIf { it.isNotBlank() && it != "null" }
    )

    private fun hashPassword(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val iterations = 120_000
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256)
        val hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        spec.clearPassword()
        return "PBKDF2\$$iterations\$${Base64.encodeToString(salt, Base64.NO_WRAP)}\$${Base64.encodeToString(hash, Base64.NO_WRAP)}"
    }

    private fun verifyPassword(password: String, saved: String): Boolean {
        return runCatching {
            val parts = saved.split('$')
            if (parts.size != 4 || parts[0] != "PBKDF2") return false
            val iterations = parts[1].toInt()
            val salt = Base64.decode(parts[2], Base64.NO_WRAP)
            val expected = Base64.decode(parts[3], Base64.NO_WRAP)
            val spec = PBEKeySpec(password.toCharArray(), salt, iterations, expected.size * 8)
            val actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            spec.clearPassword()
            MessageDigest.isEqual(expected, actual)
        }.getOrDefault(false)
    }

    companion object {
        private const val KEY_USERS = "local_users_json"
        private const val KEY_BOOKINGS = "local_bookings_json"
    }
}
