package com.example.nbk_travels.ui.screens

import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.example.nbk_travels.R
import com.example.nbk_travels.ui.NbkTheme
import com.example.nbk_travels.ui.Ui

object AuthScreens {
    fun splash(context: Context): View {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding(Ui.dp(context, 22), Ui.dp(context, 22), Ui.dp(context, 22), Ui.dp(context, 22))
        }
        root.addView(Ui.logo(context, navyBackground = false, height = 150), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(context, 150)))
        root.addView(Ui.space(context, 34))
        root.addView(android.widget.ProgressBar(context).apply {
            indeterminateTintList = android.content.res.ColorStateList.valueOf(NbkTheme.BLUE)
        }, LinearLayout.LayoutParams(Ui.dp(context, 34), Ui.dp(context, 34)))
        root.addView(Ui.space(context, 18))
        root.addView(Ui.text(context, "Connecting you to your destination...", 13f, NbkTheme.NAVY).apply { gravity = Gravity.CENTER })
        return root
    }

    fun onboarding(context: Context, onGetStarted: () -> Unit, onSignIn: () -> Unit): View {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(NbkTheme.DEEP_NAVY)
            setPadding(Ui.dp(context, 22), Ui.dp(context, 14), Ui.dp(context, 22), Ui.dp(context, 24))
        }
        root.addView(Ui.logo(context, navyBackground = true, height = 74))
        root.addView(Ui.space(context, 8))
        root.addView(ImageView(context).apply {
            setImageResource(R.drawable.nbk_travel_hero)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = "NBK Travel shuttle van and road illustration"
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(Ui.space(context, 16))
        root.addView(Ui.text(context, "Welcome to", 24f, Color.WHITE, true).apply { gravity = Gravity.CENTER })
        root.addView(Ui.text(context, "NBK Travel", 28f, NbkTheme.GOLD, true).apply { gravity = Gravity.CENTER })
        root.addView(Ui.space(context, 8))
        root.addView(Ui.text(context, "Book reliable, safe and comfortable shuttle services with ease.", 15f, Color.WHITE).apply {
            gravity = Gravity.CENTER
            setPadding(Ui.dp(context, 10), 0, Ui.dp(context, 10), 0)
        })
        root.addView(Ui.space(context, 16))
        root.addView(Ui.text(context, "â—   â—   â—", 12f, NbkTheme.GOLD).apply { gravity = Gravity.CENTER })
        root.addView(Ui.space(context, 12))
        root.addView(Ui.button(context, "Get Started  â†’", Ui.ButtonStyle.GOLD, onGetStarted))
        root.addView(Ui.button(context, "I already have an account Â· Sign in", Ui.ButtonStyle.OUTLINE, onSignIn).apply {
            backgroundTintList = android.content.res.ColorStateList.valueOf(NbkTheme.DEEP_NAVY)
            strokeColor = android.content.res.ColorStateList.valueOf(0x667FA7D7)
            setTextColor(Color.WHITE)
        })
        return root
    }

    fun login(
        context: Context,
        onLogin: (String, String) -> Unit,
        onRegister: () -> Unit,
        onForgot: () -> Unit,
        onGoogle: () -> Unit
    ): View {
        val content = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(NbkTheme.background(context)) }
        content.addView(brandHeader(context, "Customer Login", "Access your account"))
        val body = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(context, 22), Ui.dp(context, 20), Ui.dp(context, 22), Ui.dp(context, 24))
        }
        val email = Ui.labeledInput(context, body, "Email Address", "you@example.com", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        email.maxLines = 1
        val password = Ui.labeledInput(context, body, "Password", "Enter your password", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        password.maxLines = 1
        body.addView(Ui.text(context, "Forgot password?", 13f, NbkTheme.BLUE, true).apply {
            gravity = Gravity.END
            setPadding(0, 0, 0, Ui.dp(context, 8))
            setOnClickListener { onForgot() }
        })
        body.addView(Ui.space(context, 2))
        body.addView(Ui.button(context, "Sign in", Ui.ButtonStyle.PRIMARY) { onLogin(email.text.toString(), password.text.toString()) })
        body.addView(Ui.space(context, 8))
        body.addView(Ui.text(context, "OR", 12f, NbkTheme.secondaryText(context)).apply {
            gravity = Gravity.CENTER
            setPadding(0, Ui.dp(context, 5), 0, Ui.dp(context, 5))
        })
        body.addView(Ui.button(context, "G   Continue with Google", Ui.ButtonStyle.OUTLINE, onGoogle))
        body.addView(Ui.space(context, 12))
        body.addView(Ui.text(context, "Don't have an account?  Create account", 13f, NbkTheme.BLUE, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, Ui.dp(context, 10), 0, Ui.dp(context, 8))
            setOnClickListener { onRegister() }
        })
        body.addView(Ui.text(context, "By continuing, you agree to NBK Travel's terms and privacy policy.", 11f, NbkTheme.secondaryText(context)).apply { gravity = Gravity.CENTER })
        content.addView(body)
        return Ui.scroll(context, content)
    }

    fun register(
        context: Context,
        onRegister: (String, String, String, String, String, Boolean) -> Unit,
        onLogin: () -> Unit
    ): View {
        val content = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(NbkTheme.background(context)) }
        content.addView(brandHeader(context, "Register", "Create your account"))
        val body = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(context, 22), Ui.dp(context, 18), Ui.dp(context, 22), Ui.dp(context, 24))
        }
        val name = Ui.labeledInput(context, body, "Full Name", "Enter your full name")
        val email = Ui.labeledInput(context, body, "Email Address", "you@example.com", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val phone = Ui.labeledInput(context, body, "Phone Number", "e.g. 082 555 0100", InputType.TYPE_CLASS_PHONE)
        val password = Ui.labeledInput(context, body, "Password", "At least 8 characters", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val confirm = Ui.labeledInput(context, body, "Confirm Password", "Enter password again", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val terms = CheckBox(context).apply {
            text = "I agree to the Terms of Service and Privacy Policy"
            textSize = 12f
            setTextColor(NbkTheme.text(context))
            buttonTintList = android.content.res.ColorStateList.valueOf(NbkTheme.BLUE)
            setPadding(0, Ui.dp(context, 4), 0, Ui.dp(context, 8))
        }
        body.addView(terms)
        body.addView(Ui.button(context, "Create account", Ui.ButtonStyle.PRIMARY) {
            onRegister(name.text.toString(), email.text.toString(), phone.text.toString(), password.text.toString(), confirm.text.toString(), terms.isChecked)
        })
        body.addView(Ui.space(context, 8))
        body.addView(Ui.card(context, 12).apply {
            addView(Ui.text(context, "Account verification", 13f, NbkTheme.NAVY, true))
            addView(Ui.text(context, "New registrations must be verified by NBK Travel before booking access is enabled.", 12f, NbkTheme.secondaryText(context)))
        })
        body.addView(Ui.text(context, "Already have an account?  Sign in", 13f, NbkTheme.BLUE, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, Ui.dp(context, 12), 0, Ui.dp(context, 8))
            setOnClickListener { onLogin() }
        })
        content.addView(body)
        return Ui.scroll(context, content)
    }

    fun forgotPassword(context: Context, onSubmit: (String) -> Unit, onBack: () -> Unit): View {
        val content = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(NbkTheme.background(context)) }
        content.addView(Ui.topBar(context, "Forgot Password", onBack))
        val body = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(Ui.dp(context, 22), Ui.dp(context, 24), Ui.dp(context, 22), Ui.dp(context, 24)) }
        body.addView(Ui.title(context, "Reset your password"))
        body.addView(Ui.subtitle(context, "Enter the email address connected to your NBK Travel account. If password-reset email is enabled, you will receive a reset link."))
        val email = Ui.labeledInput(context, body, "Email Address", "you@example.com", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        body.addView(Ui.button(context, "Request reset link", Ui.ButtonStyle.PRIMARY) { onSubmit(email.text.toString()) })
        content.addView(body)
        return Ui.scroll(context, content)
    }

    private fun brandHeader(context: Context, title: String, subtitle: String): View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(Ui.dp(context, 22), Ui.dp(context, 14), Ui.dp(context, 22), Ui.dp(context, 22))
        setBackgroundResource(R.drawable.bg_navy_header)
        addView(Ui.logo(context, navyBackground = true, height = 92))
        addView(Ui.space(context, 4))
        addView(Ui.text(context, title, 21f, Color.WHITE, true).apply { gravity = Gravity.CENTER })
        addView(Ui.text(context, subtitle, 12f, 0xFFE0EAF7.toInt()).apply { gravity = Gravity.CENTER })
    }
}
