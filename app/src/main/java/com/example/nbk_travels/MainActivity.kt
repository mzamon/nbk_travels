package com.example.nbk_travels

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AlertDialog
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.nbk_travels.data.AppRepository
import com.example.nbk_travels.data.Booking
import com.example.nbk_travels.data.BookingDraft
import com.example.nbk_travels.data.OperationResult
import com.example.nbk_travels.data.SessionStore
import com.example.nbk_travels.data.UserProfile
import com.example.nbk_travels.data.remote.OpenMeteoApi
import com.example.nbk_travels.data.repository.WeatherRepository
import com.example.nbk_travels.ui.NbkTheme
import com.example.nbk_travels.ui.Ui
import com.example.nbk_travels.ui.screens.AuthScreens
import com.example.nbk_travels.ui.screens.BookingScreens
import com.example.nbk_travels.ui.screens.HomeScreen
import com.example.nbk_travels.ui.screens.SettingsScreens
import com.example.nbk_travels.util.InputValidator
import com.example.nbk_travels.utils.FareCalculator
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private enum class Screen { SPLASH, ONBOARDING, LOGIN, REGISTER, FORGOT_PASSWORD, HOME, BOOKINGS, BOOKING_FORM, BOOKING_DETAILS, SETTINGS, PROFILE, HELP, ABOUT }

    private lateinit var repository: AppRepository
    private lateinit var sessionStore: SessionStore
    private var currentScreen = Screen.SPLASH
    private var currentUser: UserProfile? = null
    private var bookings: List<Booking> = emptyList()
    private var selectedBookingId: String? = null
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private val weatherRepository = WeatherRepository()
    private val openMeteoApi = OpenMeteoApi()
    private var splashTransitionPending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = AppRepository(applicationContext)
        sessionStore = SessionStore(applicationContext)
        currentUser = repository.currentUser()
        applyAppearance(currentUser?.darkMode ?: false)
        configureSystemBars(transparent = false, lightStatusBars = false)

        val restoredName = savedInstanceState?.getString(STATE_SCREEN)
        currentScreen = restoredName?.let { runCatching { Screen.valueOf(it) }.getOrNull() } ?: Screen.SPLASH
        selectedBookingId = savedInstanceState?.getString(STATE_BOOKING_ID)

        onBackPressedDispatcher.addCallback(this) {
            when (currentScreen) {
                Screen.SPLASH -> isEnabled = false
                Screen.ONBOARDING -> finish()
                Screen.LOGIN -> navigate(Screen.ONBOARDING)
                Screen.REGISTER -> navigate(Screen.LOGIN)
                Screen.FORGOT_PASSWORD -> navigate(Screen.LOGIN)
                Screen.HOME -> showExitDialog()
                Screen.BOOKINGS -> navigate(Screen.HOME)
                Screen.BOOKING_FORM -> navigate(Screen.HOME)
                Screen.BOOKING_DETAILS -> navigate(Screen.BOOKINGS)
                Screen.SETTINGS -> navigate(Screen.HOME)
                Screen.PROFILE -> navigate(Screen.SETTINGS)
                Screen.HELP -> navigate(Screen.HOME)
                Screen.ABOUT -> navigate(Screen.SETTINGS)
            }
        }

        render()
        if (currentScreen == Screen.SPLASH) {
            splashTransitionPending = true
            handler.postDelayed({
                if (!isFinishing && splashTransitionPending && currentScreen == Screen.SPLASH) {
                    splashTransitionPending = false
                    currentUser = repository.currentUser()
                    navigate(if (currentUser != null) Screen.HOME else Screen.ONBOARDING)
                    if (currentUser != null) refreshBookings(showErrors = false)
                }
            }, 1400)
        } else {
            render()
            if (currentScreen == Screen.HOME || currentScreen == Screen.BOOKINGS || currentScreen == Screen.BOOKING_DETAILS) refreshBookings(showErrors = false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_SCREEN, currentScreen.name)
        outState.putString(STATE_BOOKING_ID, selectedBookingId)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        splashTransitionPending = false
        handler.removeCallbacksAndMessages(null)
        if (isFinishing) executor.shutdown()
        super.onDestroy()
    }

    private fun navigate(screen: Screen) {
        currentScreen = screen
        render()
        if (screen == Screen.HOME || screen == Screen.BOOKINGS || screen == Screen.BOOKING_DETAILS) {
            refreshBookings(showErrors = false)
        }
    }

    private fun render() {
        val user = currentUser ?: repository.currentUser().also { currentUser = it }
        val view = when (currentScreen) {
            Screen.SPLASH -> AuthScreens.splash(this)
            Screen.ONBOARDING -> AuthScreens.onboarding(this, { navigate(Screen.REGISTER) }, { navigate(Screen.LOGIN) })
            Screen.LOGIN -> AuthScreens.login(
                this,
                onLogin = ::attemptLogin,
                onRegister = { navigate(Screen.REGISTER) },
                onForgot = { navigate(Screen.FORGOT_PASSWORD) },
                onGoogle = ::startGoogleSignIn
            )
            Screen.REGISTER -> AuthScreens.register(
                this,
                onRegister = ::attemptRegister,
                onLogin = { navigate(Screen.LOGIN) }
            )
            Screen.FORGOT_PASSWORD -> AuthScreens.forgotPassword(
                this,
                onSubmit = ::requestPasswordReset,
                onBack = { navigate(Screen.LOGIN) }
            )
            Screen.HOME -> if (user != null) HomeScreen.home(
                this, user, bookings,
                onBook = { openBookingForm() },
                onBookings = { navigate(Screen.BOOKINGS) },
                onSettings = { navigate(Screen.SETTINGS) },
                onHelp = { navigate(Screen.HELP) },
                onAbout = { navigate(Screen.ABOUT) },
                onProfile = { navigate(Screen.PROFILE) },
                onMenu = ::showNavigationMenu,
                onNavigate = ::handleBottomNavigation,
                onBookingSelected = ::openBookingDetails
            ) else AuthScreens.login(this, ::attemptLogin, { navigate(Screen.REGISTER) }, { navigate(Screen.FORGOT_PASSWORD) }, ::startGoogleSignIn)
            Screen.BOOKINGS -> if (user != null) BookingScreens.bookings(
                this, bookings,
                onOpen = ::openBookingDetails,
                onNewBooking = { openBookingForm() },
                onNavigate = ::handleBottomNavigation,
                onBack = { navigate(Screen.HOME) }
            ) else AuthScreens.login(this, ::attemptLogin, { navigate(Screen.REGISTER) }, { navigate(Screen.FORGOT_PASSWORD) }, ::startGoogleSignIn)
            Screen.BOOKING_FORM -> if (user != null) BookingScreens.createBooking(
                this,
                onSubmit = ::attemptBooking,
                onEstimate = ::estimateFare,
                onWeather = ::checkWeather,
                onBack = { navigate(Screen.HOME) }
            ) else AuthScreens.login(this, ::attemptLogin, { navigate(Screen.REGISTER) }, { navigate(Screen.FORGOT_PASSWORD) }, ::startGoogleSignIn)
            Screen.BOOKING_DETAILS -> if (user != null) BookingScreens.details(
                this,
                booking = bookings.firstOrNull { it.id == selectedBookingId },
                onCancel = ::cancelBooking,
                onBack = { navigate(Screen.BOOKINGS) },
                onNavigate = ::handleBottomNavigation
            ) else AuthScreens.login(this, ::attemptLogin, { navigate(Screen.REGISTER) }, { navigate(Screen.FORGOT_PASSWORD) }, ::startGoogleSignIn)
            Screen.SETTINGS -> if (user != null) SettingsScreens.settings(
                this, user,
                onProfile = { navigate(Screen.PROFILE) },
                onChangePassword = ::showChangePasswordDialog,
                onNotifications = { checked -> updateSettings(notifications = checked) },
                onDarkMode = { checked -> updateSettings(darkMode = checked) },
                onLanguage = ::showLanguageInfo,
                onPrivacy = ::showPrivacyDialog,
                onHelp = { navigate(Screen.HELP) },
                onAbout = { navigate(Screen.ABOUT) },
                onLogout = ::confirmLogout,
                onNavigate = ::handleBottomNavigation,
                onBack = { navigate(Screen.HOME) }
            ) else AuthScreens.login(this, ::attemptLogin, { navigate(Screen.REGISTER) }, { navigate(Screen.FORGOT_PASSWORD) }, ::startGoogleSignIn)
            Screen.PROFILE -> if (user != null) SettingsScreens.profile(
                this, user,
                onSave = ::saveProfile,
                onBack = { navigate(Screen.SETTINGS) }
            ) else AuthScreens.login(this, ::attemptLogin, { navigate(Screen.REGISTER) }, { navigate(Screen.FORGOT_PASSWORD) }, ::startGoogleSignIn)
            Screen.HELP -> SettingsScreens.help(this, { navigate(Screen.HOME) }, ::handleBottomNavigation)
            Screen.ABOUT -> SettingsScreens.about(this) { navigate(Screen.SETTINGS) }
        }
        setContentView(view)
        val baseLeft = view.paddingLeft
        val baseTop = view.paddingTop
        val baseRight = view.paddingRight
        val baseBottom = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            target.setPadding(
                baseLeft + systemBars.left,
                baseTop + systemBars.top,
                baseRight + systemBars.right,
                baseBottom + systemBars.bottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(view)
        configureSystemBars(
            transparent = true,
            lightStatusBars = currentScreen != Screen.ONBOARDING
        )
    }

    private fun attemptLogin(email: String, password: String) {
        val emailError = InputValidator.validateEmail(email)
        if (emailError != null) { toast(emailError); return }
        if (password.isBlank()) { toast("Enter your password."); return }
        runAsync({ repository.login(email, password) }) { outcome ->
            outcome.onSuccess { result ->
                if (result.success && result.user != null) {
                    currentUser = result.user
                    applyAppearance(result.user.darkMode)
                    toast(result.message)
                    navigate(Screen.HOME)
                } else toast(result.message)
            }.onFailure { error -> toast(error.message ?: "Sign-in failed. Please try again.") }
        }
    }

    private fun attemptRegister(fullName: String, email: String, phone: String, password: String, confirm: String, acceptedTerms: Boolean) {
        val message = InputValidator.validateName(fullName)
            ?: InputValidator.validateEmail(email)
            ?: InputValidator.validatePhone(phone)
            ?: InputValidator.validatePassword(password)
            ?: InputValidator.validateConfirmPassword(password, confirm)
        if (message != null) { toast(message); return }
        if (!acceptedTerms) { toast("Please accept the Terms of Service and Privacy Policy."); return }
        runAsync({ repository.register(fullName, email, phone, password) }) { outcome ->
            outcome.onSuccess { result ->
                toast(result.message)
                if (result.success) navigate(Screen.LOGIN)
            }.onFailure { error -> toast(error.message ?: "Registration failed. Please try again.") }
        }
    }

    private fun requestPasswordReset(email: String) {
        runAsync({ repository.requestPasswordReset(email) }) { outcome ->
            outcome.onSuccess { result -> toast(result.message) }
                .onFailure { error -> toast(error.message ?: "Password reset request failed.") }
        }
    }

    private fun openBookingForm() {
        val user = currentUser ?: repository.currentUser()
        if (user == null) { navigate(Screen.LOGIN); return }
        if (user.status != "verified") {
            toast("Your account must be verified before you can book.")
            return
        }
        navigate(Screen.BOOKING_FORM)
    }

    private fun attemptBooking(draft: BookingDraft) {
        val error = InputValidator.validateBooking(draft.pickup, draft.dropoff, draft.date, draft.time, draft.passengers, draft.fare.toString())
        if (error != null) { toast(error); return }
        val user = currentUser ?: return
        runAsync({ repository.createBooking(user, draft) }) { outcome ->
            outcome.onSuccess { result ->
                if (result.success) {
                    toast(result.message)
                    selectedBookingId = result.booking?.id
                    navigate(Screen.BOOKINGS)
                } else toast(result.message)
            }.onFailure { error -> toast(error.message ?: "Booking could not be submitted.") }
        }
    }

    private fun estimateFare(
            pickup: String,
            dropoff: String,
            passengers: Int,
            onResult: (Double) -> Unit
        ) {
            val validation = InputValidator.validateBooking(
                pickup, dropoff, java.time.LocalDate.now().plusDays(1).toString(), "00:00", passengers, "250"
            )
            if (validation != null && validation !in listOf("Choose a valid travel date.", "Choose a valid travel time.")) {
                toast(validation)
                return
            }
            runAsync({
                val start = openMeteoApi.geocodeTown(pickup)
                val end = openMeteoApi.geocodeTown(dropoff)
                FareCalculator.calculateFare(
                    FareCalculator.haversineDistanceKm(
                        start.latitude, start.longitude, end.latitude, end.longitude
                    ),
                    passengers
                )
            }) { result ->
                result.onSuccess(onResult)
                    .onFailure { error -> toast(error.message ?: "Fare estimate failed.") }
            }
        }

    private fun checkWeather(dropoff: String, date: String, onResult: (String) -> Unit) {
            if (dropoff.trim().length < 2) {
                toast("Enter a drop-off location first.")
                return
            }
            runAsync({ weatherRepository.getWeather(dropoff, date) }) { result ->
                result.onSuccess { weather ->
                    onResult(
                        "$dropoff on ${weather.date}: high ${weather.highCelsius.toInt()}°C, " +
                            "low ${weather.lowCelsius.toInt()}°C, ${weather.rainProbability}% chance of rain"
                    )
                }.onFailure { error -> toast(error.message ?: "Weather lookup failed.") }
            }
    }

    private fun openBookingDetails(bookingId: String) {
        selectedBookingId = bookingId
        navigate(Screen.BOOKING_DETAILS)
    }

    private fun cancelBooking(bookingId: String) {
        val user = currentUser ?: return
        runAsync({ repository.cancelBooking(user, bookingId) }) { outcome ->
            outcome.onSuccess { result ->
                toast(result.message)
                if (result.success) {
                    currentScreen = Screen.BOOKINGS
                    render()
                    refreshBookings(showErrors = false)
                }
            }.onFailure { error -> toast(error.message ?: "Booking cancellation failed.") }
        }
    }

    private fun refreshBookings(showErrors: Boolean) {
        val user = currentUser ?: return
        runAsync({ repository.listBookings(user) }) { result ->
            result.onSuccess { rows ->
                bookings = rows
                if (currentScreen in listOf(Screen.HOME, Screen.BOOKINGS, Screen.BOOKING_DETAILS)) render()
            }.onFailure { error ->
                if (showErrors) toast(error.message ?: "Could not load bookings.")
            }
        }
    }

    private fun saveProfile(fullName: String, phone: String) {
        val user = currentUser ?: return
        runAsync({ repository.updateProfile(user, fullName, phone) }) { outcome ->
            outcome.onSuccess { result ->
                if (result.success && result.user != null) {
                    currentUser = result.user
                    toast(result.message)
                    navigate(Screen.SETTINGS)
                } else toast(result.message)
            }.onFailure { error -> toast(error.message ?: "Profile update failed.") }
        }
    }

    private fun updateSettings(notifications: Boolean? = null, darkMode: Boolean? = null, language: String? = null) {
        val user = currentUser ?: return
        runAsync({
            repository.updateSettings(
                user,
                notificationsEnabled = notifications ?: user.notificationsEnabled,
                darkMode = darkMode ?: user.darkMode,
                language = language ?: user.language
            )
        }) { outcome ->
            outcome.onSuccess { updated ->
                currentUser = updated
                if (darkMode != null) applyAppearance(updated.darkMode)
                render()
                if (language != null) toast("Language preference saved: $language. Interface copy is currently English.")
            }.onFailure { error -> toast(error.message ?: "Could not save settings.") }
        }
    }

    private fun showChangePasswordDialog() {
        val user = currentUser ?: return
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(this@MainActivity, 22), Ui.dp(this@MainActivity, 8), Ui.dp(this@MainActivity, 22), 0)
        }
        val current = Ui.input(this, "Current password", android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val newPassword = Ui.input(this, "New password (8+ characters)", android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val confirm = Ui.input(this, "Confirm new password", android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)
        box.addView(Ui.label(this, "Current Password")); box.addView(current)
        box.addView(Ui.label(this, "New Password")); box.addView(newPassword)
        box.addView(Ui.label(this, "Confirm New Password")); box.addView(confirm)
        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save Password", null)
            .create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val currentText = current.text.toString()
                        val next = newPassword.text.toString()
                        val confirmText = confirm.text.toString()
                        val error = InputValidator.validatePassword(next) ?: InputValidator.validateConfirmPassword(next, confirmText)
                        if (currentText.isBlank()) { current.error = "Enter your current password."; return@setOnClickListener }
                        if (error != null) { newPassword.error = error; return@setOnClickListener }
                        runAsync({ repository.changePassword(user, currentText, next) }) { outcome ->
                            outcome.onSuccess { result ->
                                toast(result.message)
                                if (result.success) dialog.dismiss()
                            }.onFailure { error -> toast(error.message ?: "Password update failed.") }
                        }
                    }
                }
                dialog.show()
            }
    }

    private fun showLanguageInfo() {
        val user = currentUser ?: return
        val languages = arrayOf("English", "Afrikaans", "isiZulu", "Sepedi", "Setswana")
        val selected = languages.indexOf(user.language).coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle("App Language")
            .setSingleChoiceItems(languages, selected) { dialog, which ->
                dialog.dismiss()
                updateSettings(language = languages[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPrivacyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Privacy & Security")
            .setMessage("Passwords in local prototype mode are stored as salted PBKDF2 hashes, not as plain text. In online mode, authentication and booking data are sent to your configured HTTPS API. Do not deploy online mode until your server validates sessions, enforces authorization, and stores passwords using a secure server-side password hash.")
            .setPositiveButton("Understood", null)
            .show()
    }

    private fun showNavigationMenu() {
        val items = arrayOf("Home", "My Bookings", "Book a Shuttle", "Profile", "Settings", "Help & Support", "About NBK Travel", "Log Out")
        AlertDialog.Builder(this)
            .setTitle("NBK Travel")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> navigate(Screen.HOME)
                    1 -> navigate(Screen.BOOKINGS)
                    2 -> openBookingForm()
                    3 -> navigate(Screen.PROFILE)
                    4 -> navigate(Screen.SETTINGS)
                    5 -> navigate(Screen.HELP)
                    6 -> navigate(Screen.ABOUT)
                    7 -> confirmLogout()
                }
            }
            .show()
    }

    private fun handleBottomNavigation(target: String) {
        when (target.lowercase()) {
            "home" -> navigate(Screen.HOME)
            "bookings" -> navigate(Screen.BOOKINGS)
            "settings" -> navigate(Screen.SETTINGS)
            "help" -> navigate(Screen.HELP)
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Log out of NBK Travel?")
            .setMessage("You will need to sign in again to view your bookings.")
            .setNegativeButton("Stay signed in", null)
            .setPositiveButton("Log Out") { _, _ ->
                repository.logout()
                currentUser = null
                bookings = emptyList()
                selectedBookingId = null
                navigate(Screen.LOGIN)
            }
            .show()
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Close NBK Travel?")
            .setMessage("You are signed in. You can close the app and return later.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Close") { _, _ -> finish() }
            .show()
    }

    private fun startGoogleSignIn() {
        val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        if (webClientId.isBlank()) {
            toast("Google SSO needs NBK_GOOGLE_WEB_CLIENT_ID configured in gradle.properties.")
            return
        }
        lifecycleScope.launch {
            val nonce = createNonce()
            try {
                val credentialManager = CredentialManager.create(this@MainActivity)
                val option = GetSignInWithGoogleOption.Builder(webClientId).setNonce(nonce).build()
                val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                val response = credentialManager.getCredential(context = this@MainActivity, request = request)
                val credential = response.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    runAsync({ repository.loginWithGoogle(idToken, nonce) }) { outcome ->
                        outcome.onSuccess { result ->
                            if (result.success && result.user != null) {
                                currentUser = result.user
                                applyAppearance(result.user.darkMode)
                                toast(result.message)
                                navigate(Screen.HOME)
                            } else toast(result.message)
                        }.onFailure { error -> toast(error.message ?: "Google sign-in failed.") }
                    }
                } else {
                    toast("Google did not return a supported sign-in credential.")
                }
            } catch (error: GetCredentialException) {
                toast(error.message ?: "Google sign-in was cancelled or unavailable.")
            } catch (error: Exception) {
                toast(error.message ?: "Google sign-in failed. Check your OAuth configuration.")
            }
        }
    }

    private fun createNonce(): String {
        val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        return Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
    }

    private fun applyAppearance(darkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

    @Suppress("DEPRECATION")
    private fun configureSystemBars(transparent: Boolean, lightStatusBars: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = if (transparent) android.graphics.Color.TRANSPARENT else NbkTheme.DEEP_NAVY
        window.navigationBarColor = if (transparent) android.graphics.Color.TRANSPARENT else NbkTheme.DEEP_NAVY
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = lightStatusBars
            isAppearanceLightNavigationBars = lightStatusBars
        }
    }

    private fun <T> runAsync(work: () -> T, callback: (Result<T>) -> Unit) {
        executor.execute {
            val result = runCatching { work() }
            runOnUiThread { if (!isFinishing) callback(result) }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val STATE_SCREEN = "nbk.current.screen"
        private const val STATE_BOOKING_ID = "nbk.selected.booking.id"
    }
}
