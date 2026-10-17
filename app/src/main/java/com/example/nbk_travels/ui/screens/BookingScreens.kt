package com.example.nbk_travels.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.nbk_travels.R
import com.example.nbk_travels.data.Booking
import com.example.nbk_travels.data.BookingDraft
import com.example.nbk_travels.data.UserProfile
import com.example.nbk_travels.ui.NbkTheme
import com.example.nbk_travels.ui.Ui
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

object BookingScreens {
    fun createBooking(context: Context, onSubmit: (BookingDraft) -> Unit, onBack: () -> Unit): View {
        val root = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(NbkTheme.background(context)) }
        root.addView(Ui.topBar(context, "Book a Shuttle", onBack))
        val body = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(context, 20), Ui.dp(context, 16), Ui.dp(context, 20), Ui.dp(context, 24))
        }
        body.addView(Ui.text(context, "Where would you like to go?", 20f, NbkTheme.text(context), true))
        body.addView(Ui.subtitle(context, "Enter your trip details to create a booking request."))
        body.addView(Ui.label(context, "Pickup Location"))
        val pickup = Ui.input(context, "e.g. Sandton City")
        body.addView(pickup)
        body.addView(Ui.label(context, "Dropoff Location"))
        val dropoff = Ui.input(context, "e.g. OR Tambo International Airport")
        body.addView(dropoff)

        val today = LocalDate.now()
        body.addView(Ui.label(context, "Travel Date"))
        val date = Ui.input(context, "Select a date").apply {
            isFocusable = false
            isClickable = true
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar, 0, 0, 0)
            compoundDrawablePadding = Ui.dp(context, 10)
        }
        body.addView(date)
        date.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(context, { _, year, month, day ->
                date.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day))
            }, today.year, today.monthValue - 1, today.dayOfMonth).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
            }.show()
        }

        body.addView(Ui.label(context, "Pickup Time"))
        val time = Ui.input(context, "Select a time").apply {
            isFocusable = false
            isClickable = true
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_clock, 0, 0, 0)
            compoundDrawablePadding = Ui.dp(context, 10)
        }
        body.addView(time)
        time.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(context, { _, hour, minute ->
                time.setText(String.format(Locale.US, "%02d:%02d", hour, minute))
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
        }

        body.addView(Ui.label(context, "Passengers"))
        var passengerCount = 1
        val countLabel = Ui.text(context, "1", 16f, NbkTheme.text(context), true).apply { gravity = Gravity.CENTER }
        val stepper = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(Ui.dp(context, 12), Ui.dp(context, 4), Ui.dp(context, 12), Ui.dp(context, 4))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(NbkTheme.surface(context)); cornerRadius = Ui.dp(context, 12).toFloat(); setStroke(Ui.dp(context, 1), NbkTheme.border(context))
            }
        }
        stepper.addView(Ui.text(context, "Passengers", 14f, NbkTheme.secondaryText(context)), LinearLayout.LayoutParams(0, Ui.dp(context, 48), 1f).apply { gravity = Gravity.CENTER_VERTICAL })
        val minus = stepperControl(context, "âˆ’")
        val plus = stepperControl(context, "+")
        stepper.addView(minus)
        stepper.addView(countLabel, LinearLayout.LayoutParams(Ui.dp(context, 42), Ui.dp(context, 44)))
        stepper.addView(plus)
        body.addView(stepper, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = Ui.dp(context, 8) })
        minus.setOnClickListener { if (passengerCount > 1) passengerCount--; countLabel.text = passengerCount.toString() }
        plus.setOnClickListener { if (passengerCount < 50) passengerCount++; countLabel.text = passengerCount.toString() }

        body.addView(Ui.label(context, "Fare (ZAR)"))
        val fare = Ui.input(context, "e.g. 450.00", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        body.addView(fare)
        body.addView(Ui.text(context, "Your booking will be marked Pending until confirmed by an NBK Travel administrator.", 12f, NbkTheme.secondaryText(context)).apply {
            setPadding(0, Ui.dp(context, 6), 0, Ui.dp(context, 12))
        })
        body.addView(Ui.button(context, "Create Booking", Ui.ButtonStyle.PRIMARY) {
            val draftDate = date.text.toString().trim()
            val draftTime = time.text.toString().trim()
            val parsedFare = fare.text.toString().trim().toDoubleOrNull()
            if (parsedFare == null) {
                fare.error = "Enter a fare amount."
            } else {
                onSubmit(BookingDraft(pickup.text.toString(), dropoff.text.toString(), draftDate, draftTime, passengerCount, parsedFare))
            }
        })
        root.addView(Ui.scroll(context, body), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        return root
    }

    fun bookings(
        context: Context,
        bookings: List<Booking>,
        onOpen: (String) -> Unit,
        onNewBooking: () -> Unit,
        onNavigate: (String) -> Unit,
        onBack: () -> Unit
    ): View {
        val root = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(NbkTheme.background(context)) }
        root.addView(Ui.topBar(context, "My Bookings", onBack))
        val body = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(Ui.dp(context, 16), Ui.dp(context, 14), Ui.dp(context, 16), Ui.dp(context, 16)) }
        val filters = listOf("All", "Pending", "Confirmed", "Completed", "Cancelled")
        var selected = "All"
        val listArea = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        fun drawList() {
            listArea.removeAllViews()
            val filtered = if (selected == "All") bookings else bookings.filter { it.status.equals(selected, true) }
            if (filtered.isEmpty()) {
                listArea.addView(Ui.card(context, 20).apply {
                    gravity = Gravity.CENTER
                    addView(Ui.text(context, "No ${selected.lowercase()} bookings", 16f, NbkTheme.text(context), true).apply { gravity = Gravity.CENTER })
                    addView(Ui.text(context, "Your trip requests and their latest status will appear here.", 12f, NbkTheme.secondaryText(context)).apply {
                        gravity = Gravity.CENTER; setPadding(0, Ui.dp(context, 8), 0, Ui.dp(context, 8))
                    })
                    addView(Ui.button(context, "Book a shuttle", Ui.ButtonStyle.PRIMARY, onNewBooking))
                })
            } else {
                filtered.forEach { booking ->
                    listArea.addView(HomeScreen.bookingSummary(context, booking) { onOpen(booking.id) })
                }
            }
        }
        val chipStrip = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        filters.forEach { filter ->
            val chip = Ui.text(context, filter, 11.5f, if (filter == selected) Color.WHITE else NbkTheme.NAVY, filter == selected).apply {
                gravity = Gravity.CENTER
                setPadding(Ui.dp(context, 13), Ui.dp(context, 9), Ui.dp(context, 13), Ui.dp(context, 9))
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(if (filter == selected) NbkTheme.NAVY else NbkTheme.surface(context))
                    cornerRadius = Ui.dp(context, 18).toFloat()
                    if (filter != selected) setStroke(Ui.dp(context, 1), NbkTheme.border(context))
                }
                setOnClickListener {
                    selected = filter
                    for (i in 0 until chipStrip.childCount) {
                        val child = chipStrip.getChildAt(i) as TextView
                        val active = child.text.toString() == selected
                        child.setTextColor(if (active) Color.WHITE else NbkTheme.NAVY)
                        child.background = android.graphics.drawable.GradientDrawable().apply {
                            setColor(if (active) NbkTheme.NAVY else NbkTheme.surface(context))
                            cornerRadius = Ui.dp(context, 18).toFloat()
                            if (!active) setStroke(Ui.dp(context, 1), NbkTheme.border(context))
                        }
                    }
                    drawList()
                }
            }
            chipStrip.addView(chip, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(context, 38)).apply {
                rightMargin = Ui.dp(context, 6)
            })
        }
        val filterScroll = HorizontalScrollView(context).apply { isHorizontalScrollBarEnabled = false; addView(chipStrip) }
        body.addView(filterScroll)
        body.addView(Ui.space(context, 14))
        body.addView(listArea)
        drawList()
        val scroll = Ui.scroll(context, body)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(Ui.bottomNav(context, "bookings", onNavigate))
        return root
    }

    fun details(
        context: Context,
        booking: Booking?,
        onCancel: (String) -> Unit,
        onBack: () -> Unit,
        onNavigate: (String) -> Unit
    ): View {
        val root = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(NbkTheme.background(context)) }
        root.addView(Ui.topBar(context, "Booking Details", onBack))
        val body = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(Ui.dp(context, 18), Ui.dp(context, 16), Ui.dp(context, 18), Ui.dp(context, 20)) }
        if (booking == null) {
            body.addView(Ui.title(context, "Booking not found"))
            body.addView(Ui.subtitle(context, "It may have been removed or is not available for this account."))
        } else {
            body.addView(Ui.card(context, 0).apply {
                addView(android.widget.ImageView(context).apply {
                    setImageResource(R.drawable.nbk_travel_hero)
                    scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    contentDescription = "NBK Travel shuttle van"
                }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(context, 132)))
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(Ui.dp(context, 14), Ui.dp(context, 14), Ui.dp(context, 14), Ui.dp(context, 6))
                    addView(Ui.text(context, "${booking.id}", 15f, NbkTheme.text(context), true), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                    addView(Ui.statusPill(context, booking.status))
                })
                addView(Ui.divider(context))
                addDetailRow(context, "Pickup Location", booking.pickup, R.drawable.ic_location_pin)
                addDetailRow(context, "Dropoff Location", booking.dropoff, R.drawable.ic_route)
                addDetailRow(context, "Date", booking.date, R.drawable.ic_calendar)
                addDetailRow(context, "Time", booking.time, R.drawable.ic_clock)
                addDetailRow(context, "Passengers", booking.passengers.toString(), R.drawable.ic_people)
                addDetailRow(context, "Fare", "R${"%.2f".format(booking.fare)}", R.drawable.ic_info)
                addDetailRow(context, "Driver", booking.driverName ?: "Assigned by admin after confirmation", R.drawable.ic_person)
                addDetailRow(context, "Vehicle", booking.vehicleName ?: "Assigned by admin after confirmation", R.drawable.ic_bus)
                addView(Ui.space(context, 10))
            })
            if (booking.status.lowercase() in listOf("pending", "confirmed")) {
                body.addView(Ui.button(context, "Cancel Booking", Ui.ButtonStyle.DANGER) {
                    AlertDialog.Builder(context)
                        .setTitle("Cancel this booking?")
                        .setMessage("This will mark ${booking.id} as cancelled.")
                        .setNegativeButton("Keep booking", null)
                        .setPositiveButton("Cancel booking") { _, _ -> onCancel(booking.id) }
                        .show()
                })
            }
        }
        root.addView(Ui.scroll(context, body), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(Ui.bottomNav(context, "bookings", onNavigate))
        return root
    }

    private fun LinearLayout.addDetailRow(context: Context, title: String, value: String, iconRes: Int) {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(Ui.dp(context, 14), Ui.dp(context, 8), Ui.dp(context, 14), Ui.dp(context, 8))
        }
        row.addView(android.widget.ImageView(context).apply {
            setImageResource(iconRes)
            imageTintList = android.content.res.ColorStateList.valueOf(NbkTheme.BLUE)
        }, LinearLayout.LayoutParams(Ui.dp(context, 22), Ui.dp(context, 22)))
        val texts = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(Ui.dp(context, 10), 0, 0, 0) }
        texts.addView(Ui.text(context, title, 11f, NbkTheme.secondaryText(context)))
        texts.addView(Ui.text(context, value, 14f, NbkTheme.text(context), true))
        row.addView(texts, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        addView(row)
        addView(Ui.divider(context))
    }

    private fun stepperControl(context: Context, label: String): TextView = Ui.text(context, label, 18f, NbkTheme.NAVY, true).apply {
        gravity = Gravity.CENTER
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(NbkTheme.PALE_BLUE)
            cornerRadius = Ui.dp(context, 10).toFloat()
        }
        layoutParams = LinearLayout.LayoutParams(Ui.dp(context, 38), Ui.dp(context, 38))
    }
}
