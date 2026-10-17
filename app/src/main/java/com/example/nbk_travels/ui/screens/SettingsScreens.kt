package com.example.nbk_travels.ui.screens

import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.nbk_travels.R
import com.example.nbk_travels.data.UserProfile
import com.example.nbk_travels.ui.NbkTheme
import com.example.nbk_travels.ui.Ui

object SettingsScreens {
    fun settings(
        context: Context,
        user: UserProfile,
        onProfile: () -> Unit,
        onChangePassword: () -> Unit,
        onNotifications: (Boolean) -> Unit,
        onDarkMode: (Boolean) -> Unit,
        onLanguage: () -> Unit,
        onPrivacy: () -> Unit,
        onHelp: () -> Unit,
        onAbout: () -> Unit,
        onLogout: () -> Unit,
        onNavigate: (String) -> Unit,
        onBack: () -> Unit
    ): View {
        val root = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(NbkTheme.background(context)) }
        root.addView(Ui.topBar(context, "Settings", onBack))
        val body = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(context, 18), Ui.dp(context, 15), Ui.dp(context, 18), Ui.dp(context, 18))
        }
        body.addView(Ui.card(context, 14).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val initials = user.fullName.trim().split(Regex("\\s+")).take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }.ifBlank { "NB" }
            addView(Ui.text(context, initials, 20f, Color.WHITE, true).apply {
                gravity = Gravity.CENTER
                background = android.graphics.drawable.GradientDrawable().apply { setColor(NbkTheme.BLUE); shape = android.graphics.drawable.GradientDrawable.OVAL }
            }, LinearLayout.LayoutParams(Ui.dp(context, 56), Ui.dp(context, 56)))
            val identity = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            identity.addView(Ui.text(context, user.fullName, 16f, NbkTheme.text(context), true))
            identity.addView(Ui.text(context, user.email, 12f, NbkTheme.secondaryText(context)))
            addView(identity, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = Ui.dp(context, 12) })
            addView(Ui.text(context, "Edit", 12f, NbkTheme.BLUE, true).apply { setOnClickListener { onProfile() } })
            setOnClickListener { onProfile() }
        })

        body.addView(Ui.text(context, "ACCOUNT", 11f, NbkTheme.secondaryText(context), true).apply {
            setPadding(Ui.dp(context, 2), Ui.dp(context, 5), 0, Ui.dp(context, 6))
        })
        body.addView(settingRow(context, R.drawable.ic_person, "Edit Profile", "Update your name and phone number", "â€º", onProfile))
        body.addView(settingRow(context, R.drawable.ic_lock, "Change Password", "Keep your account secure", "â€º", onChangePassword))
        body.addView(Ui.space(context, 6))
        body.addView(Ui.text(context, "PREFERENCES", 11f, NbkTheme.secondaryText(context), true).apply {
            setPadding(Ui.dp(context, 2), Ui.dp(context, 5), 0, Ui.dp(context, 6))
        })
        body.addView(Ui.card(context, 14).apply {
            addView(switchRow(context, "Notifications", "Booking status and service updates", user.notificationsEnabled, onNotifications))
            addView(Ui.divider(context))
            addView(switchRow(context, "Dark Mode", "Use a darker app appearance", user.darkMode, onDarkMode))
        })
        body.addView(settingRow(context, R.drawable.ic_info, "Language", "${user.language} Â· App language", "â€º", onLanguage))
        body.addView(Ui.space(context, 6))
        body.addView(Ui.text(context, "SUPPORT & INFORMATION", 11f, NbkTheme.secondaryText(context), true).apply {
            setPadding(Ui.dp(context, 2), Ui.dp(context, 5), 0, Ui.dp(context, 6))
        })
        body.addView(settingRow(context, R.drawable.ic_help, "Help & Support", "Answers to common questions", "â€º", onHelp))
        body.addView(settingRow(context, R.drawable.ic_info, "Privacy & Security", "How your account data is handled", "â€º", onPrivacy))
        body.addView(settingRow(context, R.drawable.ic_bus, "About NBK Travel", "App information and version", "â€º", onAbout))
        body.addView(Ui.space(context, 8))
        body.addView(Ui.button(context, "Log Out", Ui.ButtonStyle.DANGER, onLogout))
        body.addView(Ui.text(context, "NBK Travel Â· Version 1.0.0", 11f, NbkTheme.secondaryText(context)).apply {
            gravity = Gravity.CENTER; setPadding(0, Ui.dp(context, 10), 0, Ui.dp(context, 8))
        })
        root.addView(Ui.scroll(context, body), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(Ui.bottomNav(context, "settings", onNavigate))
        return root
    }

    fun profile(context: Context, user: UserProfile, onSave: (String, String) -> Unit, onBack: () -> Unit): View {
        val root = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(NbkTheme.background(context)) }
        root.addView(Ui.topBar(context, "Edit Profile", onBack))
        val body = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(Ui.dp(context, 22), Ui.dp(context, 22), Ui.dp(context, 22), Ui.dp(context, 24)) }
        body.addView(Ui.title(context, "Your personal details"))
        body.addView(Ui.subtitle(context, "Keep your contact details up to date for booking support."))
        val fullName = Ui.labeledInput(context, body, "Full Name", "Enter your full name", initialValue = user.fullName)
        val email = Ui.labeledInput(context, body, "Email Address", "Email address", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, user.email).apply {
            isEnabled = false
            alpha = 0.75f
        }
        body.addView(Ui.text(context, "Email address changes must be handled by account support.", 11f, NbkTheme.secondaryText(context)))
        val phone = Ui.labeledInput(context, body, "Phone Number", "e.g. 082 555 0100", InputType.TYPE_CLASS_PHONE, user.phone)
        body.addView(Ui.space(context, 10))
        body.addView(Ui.button(context, "Save Changes", Ui.ButtonStyle.PRIMARY) { onSave(fullName.text.toString(), phone.text.toString()) })
        root.addView(Ui.scroll(context, body))
        return root
    }

    fun help(context: Context, onBack: () -> Unit, onNavigate: (String) -> Unit): View {
        val root = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(NbkTheme.background(context)) }
        root.addView(Ui.topBar(context, "Help & Support", onBack))
        val body = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(Ui.dp(context, 18), Ui.dp(context, 18), Ui.dp(context, 18), Ui.dp(context, 18)) }
        body.addView(Ui.card(context, 16).apply {
            gravity = Gravity.CENTER
            addView(Ui.text(context, "Need Help?", 21f, NbkTheme.NAVY, true).apply { gravity = Gravity.CENTER })
            addView(Ui.text(context, "We're here to help make your trip simple.", 13f, NbkTheme.secondaryText(context)).apply { gravity = Gravity.CENTER })
        })
        body.addView(Ui.text(context, "Frequently Asked Questions", 17f, NbkTheme.text(context), true).apply {
            setPadding(0, Ui.dp(context, 4), 0, Ui.dp(context, 9))
        })
        faqRow(context, body, "How do I book a shuttle?", "Open Book a Shuttle, enter your pickup and destination, choose the date and time, select passenger count, enter the fare, and submit. The booking will appear as Pending until an administrator confirms it.")
        faqRow(context, body, "Why is my account pending?", "New registrations require verification by NBK Travel. You cannot create bookings until your account is verified.")
        faqRow(context, body, "How do I know if a booking is confirmed?", "Open My Bookings. Each booking shows its current status. Confirmed bookings can display the assigned driver and vehicle when those details are provided by the service.")
        faqRow(context, body, "Can I cancel a booking?", "Open the booking details and select Cancel Booking. Cancellation is available for Pending and Confirmed bookings in this prototype; your service's policy may apply.")
        faqRow(context, body, "What if I entered the wrong trip details?", "Cancel the pending request if cancellation is available and create a corrected booking. Contact NBK Travel support for changes to a confirmed trip.")
        body.addView(Ui.space(context, 10))
        body.addView(Ui.button(context, "My Bookings", Ui.ButtonStyle.OUTLINE) { onNavigate("bookings") })
        body.addView(Ui.button(context, "Account Settings", Ui.ButtonStyle.OUTLINE) { onNavigate("settings") })
        root.addView(Ui.scroll(context, body), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(Ui.bottomNav(context, "help", onNavigate))
        return root
    }

    fun about(context: Context, onBack: () -> Unit): View {
        val root = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(NbkTheme.background(context)) }
        root.addView(Ui.topBar(context, "About NBK Travel", onBack))
        val body = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(Ui.dp(context, 24), Ui.dp(context, 22), Ui.dp(context, 24), Ui.dp(context, 26))
        }
        body.addView(Ui.logo(context, navyBackground = false, height = 142))
        body.addView(Ui.space(context, 18))
        body.addView(Ui.title(context, "About NBK Travel").apply { gravity = Gravity.CENTER })
        body.addView(Ui.text(context, "NBK Travel is a shuttle booking and transport management service concept focused on making trip requests, booking status and dispatch details easier to manage.", 14f, NbkTheme.text(context)).apply {
            gravity = Gravity.CENTER
            setPadding(0, Ui.dp(context, 12), 0, Ui.dp(context, 14))
        })
        body.addView(Ui.card(context, 16).apply {
            addView(Ui.text(context, "Travel smart. Travel with NBK.", 15f, NbkTheme.NAVY, true).apply { gravity = Gravity.CENTER })
            addView(Ui.text(context, "Customer mobile application Â· Version 1.0.0", 12f, NbkTheme.secondaryText(context)).apply {
                gravity = Gravity.CENTER; setPadding(0, Ui.dp(context, 8), 0, 0)
            })
        })
        body.addView(Ui.text(context, "NBK Travels is an academic prototype. Live account verification, online booking records, and Google SSO require the hosted API configuration.", 11f, NbkTheme.secondaryText(context)).apply {
            gravity = Gravity.CENTER; setPadding(0, Ui.dp(context, 18), 0, 0)
        })
        root.addView(Ui.scroll(context, body))
        return root
    }

    private fun settingRow(context: Context, iconRes: Int, title: String, subtitle: String, trailing: String, onClick: () -> Unit): View = Ui.card(context, 12).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(android.widget.ImageView(context).apply {
            setImageResource(iconRes)
            imageTintList = android.content.res.ColorStateList.valueOf(NbkTheme.BLUE)
        }, LinearLayout.LayoutParams(Ui.dp(context, 25), Ui.dp(context, 25)))
        val texts = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(Ui.text(context, title, 14f, NbkTheme.text(context), true))
            addView(Ui.text(context, subtitle, 11f, NbkTheme.secondaryText(context)))
        }
        addView(texts, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = Ui.dp(context, 12) })
        addView(Ui.text(context, trailing, 22f, NbkTheme.secondaryText(context)))
        setOnClickListener { onClick() }
    }

    private fun switchRow(context: Context, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit): View = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val texts = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(Ui.text(context, title, 14f, NbkTheme.text(context), true))
            addView(Ui.text(context, subtitle, 11f, NbkTheme.secondaryText(context)))
        }
        addView(texts, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        addView(Switch(context).apply {
            isChecked = checked
            thumbTintList = android.content.res.ColorStateList.valueOf(if (checked) NbkTheme.BLUE else 0xFFCBD5E1.toInt())
            setOnCheckedChangeListener { _, isCheckedNow -> onChange(isCheckedNow) }
        })
    }

    private fun faqRow(context: Context, parent: LinearLayout, title: String, answer: String) {
        parent.addView(Ui.card(context, 14).apply {
            addView(Ui.row(context, Ui.text(context, title, 14f, NbkTheme.text(context), true), Ui.text(context, "+", 20f, NbkTheme.BLUE, true)))
            setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(answer)
                    .setPositiveButton("Got it", null)
                    .show()
            }
        })
    }
}
