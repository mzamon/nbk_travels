package com.example.nbk_travels.ui.screens

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.nbk_travels.R
import com.example.nbk_travels.data.Booking
import com.example.nbk_travels.data.UserProfile
import com.example.nbk_travels.ui.NbkTheme
import com.example.nbk_travels.ui.Ui

object HomeScreen {
    fun home(
        context: Context,
        user: UserProfile,
        bookings: List<Booking>,
        onBook: () -> Unit,
        onBookings: () -> Unit,
        onSettings: () -> Unit,
        onHelp: () -> Unit,
        onAbout: () -> Unit,
        onProfile: () -> Unit,
        onMenu: () -> Unit,
        onNavigate: (String) -> Unit,
        onBookingSelected: (String) -> Unit
    ): View {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(NbkTheme.background(context))
        }
        root.addView(homeHeader(context, onMenu, onProfile))
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(context, 18), Ui.dp(context, 16), Ui.dp(context, 18), Ui.dp(context, 16))
        }
        content.addView(Ui.text(context, "Hello, ${user.fullName.substringBefore(' ')} ðŸ‘‹", 23f, NbkTheme.text(context), true))
        content.addView(Ui.text(context, "Welcome to NBK Travel", 13f, NbkTheme.secondaryText(context)).apply {
            setPadding(0, 0, 0, Ui.dp(context, 12))
        })
        if (user.status != "verified") {
            content.addView(Ui.card(context, 12).apply {
                addView(Ui.text(context, "Account verification pending", 13f, 0xFF8A5A00.toInt(), true))
                addView(Ui.text(context, "Booking access becomes available after administrator verification.", 12f, NbkTheme.secondaryText(context)))
            })
        }
        content.addView(Ui.card(context, 14).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(ImageView(context).apply {
                setImageResource(R.drawable.ic_calendar)
                imageTintList = android.content.res.ColorStateList.valueOf(NbkTheme.BLUE)
                layoutParams = LinearLayout.LayoutParams(Ui.dp(context, 42), Ui.dp(context, 42))
            })
            val column = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            column.addView(Ui.text(context, "Book a Shuttle", 17f, NbkTheme.text(context), true))
            column.addView(Ui.text(context, "Plan your next trip with us", 12f, NbkTheme.secondaryText(context)))
            addView(column, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = Ui.dp(context, 12) })
            addView(Ui.text(context, "â€º", 28f, NbkTheme.BLUE, true))
            setOnClickListener { onBook() }
        })

        content.addView(Ui.text(context, "Quick Links", 17f, NbkTheme.text(context), true).apply {
            setPadding(0, Ui.dp(context, 2), 0, Ui.dp(context, 10))
        })
        val quickRow1 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        quickRow1.addView(quickLink(context, "â–¦", "My Bookings", onBookings), LinearLayout.LayoutParams(0, Ui.dp(context, 94), 1f).apply { rightMargin = Ui.dp(context, 6) })
        quickRow1.addView(quickLink(context, "âš™", "Settings", onSettings), LinearLayout.LayoutParams(0, Ui.dp(context, 94), 1f).apply { leftMargin = Ui.dp(context, 6) })
        content.addView(quickRow1)
        val quickRow2 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        quickRow2.addView(quickLink(context, "?", "Help", onHelp), LinearLayout.LayoutParams(0, Ui.dp(context, 94), 1f).apply { rightMargin = Ui.dp(context, 6) })
        quickRow2.addView(quickLink(context, "â“˜", "About", onAbout), LinearLayout.LayoutParams(0, Ui.dp(context, 94), 1f).apply { leftMargin = Ui.dp(context, 6) })
        content.addView(quickRow2)

        val hero = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(NbkTheme.DEEP_NAVY)
                cornerRadius = Ui.dp(context, 16).toFloat()
            }
            clipToOutline = true
            setOnClickListener { onBook() }
        }
        hero.addView(ImageView(context).apply {
            setImageResource(R.drawable.nbk_travel_hero)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = "NBK Travel shuttle illustration"
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(context, 100)))
        hero.addView(Ui.text(context, "Travel smart. Travel with NBK.", 14f, Color.WHITE, true).apply {
            setPadding(Ui.dp(context, 14), Ui.dp(context, 10), Ui.dp(context, 14), Ui.dp(context, 12))
        })
        content.addView(hero, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = Ui.dp(context, 8); bottomMargin = Ui.dp(context, 12)
        })

        content.addView(Ui.text(context, "Upcoming booking", 17f, NbkTheme.text(context), true).apply {
            setPadding(0, Ui.dp(context, 4), 0, Ui.dp(context, 8))
        })
        val upcoming = bookings.firstOrNull { it.status.lowercase() in listOf("pending", "confirmed") }
        if (upcoming == null) {
            content.addView(Ui.card(context, 14).apply {
                addView(Ui.text(context, "No upcoming trips yet", 14f, NbkTheme.text(context), true))
                addView(Ui.text(context, "Your next shuttle booking will appear here.", 12f, NbkTheme.secondaryText(context)))
                addView(Ui.button(context, "Book your first shuttle", Ui.ButtonStyle.PRIMARY, onBook))
            })
        } else {
            content.addView(bookingSummary(context, upcoming) { onBookingSelected(upcoming.id) })
        }
        content.addView(Ui.button(context, "View all bookings", Ui.ButtonStyle.OUTLINE, onBookings))

        val scroll = Ui.scroll(context, content)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(Ui.bottomNav(context, "home", onNavigate))
        return root
    }

    private fun homeHeader(context: Context, onMenu: () -> Unit, onProfile: () -> Unit): View = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundResource(R.drawable.bg_navy_header)
        setPadding(Ui.dp(context, 16), Ui.dp(context, 4), Ui.dp(context, 10), Ui.dp(context, 4))
        addView(Ui.logo(context, navyBackground = true, height = 52), LinearLayout.LayoutParams(0, Ui.dp(context, 56), 1f))
        addView(Ui.headerAction(context, "â˜»", onProfile).apply { textSize = 21f; contentDescription = "Profile" })
        addView(Ui.headerAction(context, "â˜°", onMenu).apply { contentDescription = "Open navigation menu" })
    }

    private fun quickLink(context: Context, icon: String, title: String, onClick: () -> Unit): View = Ui.card(context, 12).apply {
        gravity = Gravity.CENTER
        addView(Ui.text(context, icon, 25f, NbkTheme.BLUE, true).apply { gravity = Gravity.CENTER })
        addView(Ui.text(context, title, 12f, NbkTheme.text(context), true).apply {
            gravity = Gravity.CENTER
            setPadding(0, Ui.dp(context, 5), 0, 0)
        })
        setOnClickListener { onClick() }
    }

    fun bookingSummary(context: Context, booking: Booking, onClick: () -> Unit): View = Ui.card(context, 14).apply {
        val route = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(Ui.text(context, booking.pickup, 14f, NbkTheme.text(context), true), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(Ui.text(context, "â†’", 17f, NbkTheme.BLUE, true))
            addView(Ui.text(context, booking.dropoff, 14f, NbkTheme.text(context), true).apply { gravity = Gravity.END }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        addView(route)
        addView(Ui.space(context, 8))
        addView(Ui.text(context, "â–¦  ${booking.date}     â—·  ${booking.time}     â™Ÿ  ${booking.passengers} passengers", 11f, NbkTheme.secondaryText(context)))
        addView(Ui.space(context, 8))
        addView(Ui.row(context, Ui.text(context, "R${"%.2f".format(booking.fare)}", 15f, NbkTheme.NAVY, true), Ui.statusPill(context, booking.status)))
        setOnClickListener { onClick() }
    }
}
