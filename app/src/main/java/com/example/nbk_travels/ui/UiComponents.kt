package com.example.nbk_travels.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.button.MaterialButton

object Ui {
    fun dp(context: Context, value: Int): Int = (value * context.resources.displayMetrics.density).toInt()

    fun page(context: Context, padding: Int = 20): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(context, padding), dp(context, padding), dp(context, padding), dp(context, padding))
        setBackgroundColor(NbkTheme.background(context))
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    fun scroll(context: Context, child: View): ScrollView = ScrollView(context).apply {
        isFillViewport = true
        clipToPadding = false
        addView(child, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    fun text(context: Context, value: String, size: Float = 14f, color: Int = NbkTheme.text(context), bold: Boolean = false): TextView =
        TextView(context).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            includeFontPadding = true
        }

    fun title(context: Context, value: String): TextView = text(context, value, 24f, NbkTheme.text(context), true).apply {
        setPadding(0, 0, 0, dp(context, 6))
    }

    fun subtitle(context: Context, value: String): TextView = text(context, value, 14f, NbkTheme.secondaryText(context)).apply {
        setPadding(0, 0, 0, dp(context, 12))
    }

    fun label(context: Context, value: String): TextView = text(context, value, 13f, NbkTheme.text(context), true).apply {
        setPadding(0, dp(context, 10), 0, dp(context, 5))
    }

    fun space(context: Context, height: Int = 12): View = View(context).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(context, height))
    }

    fun divider(context: Context): View = View(context).apply {
        setBackgroundColor(NbkTheme.border(context))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 1)).apply {
            topMargin = dp(context, 10)
            bottomMargin = dp(context, 10)
        }
    }

    fun card(context: Context, padding: Int = 16): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(context, padding), dp(context, padding), dp(context, padding), dp(context, padding))
        background = GradientDrawable().apply {
            setColor(NbkTheme.surface(context))
            cornerRadius = dp(context, 18).toFloat()
            setStroke(dp(context, 1), NbkTheme.border(context))
        }
        elevation = dp(context, 1).toFloat()
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(context, 14)
        }
    }

    fun button(
        context: Context,
        text: String,
        style: ButtonStyle = ButtonStyle.PRIMARY,
        onClick: () -> Unit
    ): MaterialButton = MaterialButton(context).apply {
        this.text = text
        isAllCaps = false
        textSize = 14f
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        cornerRadius = dp(context, 26)
        insetTop = 0
        insetBottom = 0
        minimumHeight = dp(context, 50)
        setPadding(dp(context, 18), dp(context, 8), dp(context, 18), dp(context, 8))
        when (style) {
            ButtonStyle.PRIMARY -> {
                backgroundTintList = ColorStateList.valueOf(NbkTheme.NAVY)
                setTextColor(Color.WHITE)
            }
            ButtonStyle.GOLD -> {
                backgroundTintList = ColorStateList.valueOf(NbkTheme.GOLD)
                setTextColor(NbkTheme.DEEP_NAVY)
            }
            ButtonStyle.OUTLINE -> {
                backgroundTintList = ColorStateList.valueOf(NbkTheme.surface(context))
                strokeColor = ColorStateList.valueOf(NbkTheme.border(context))
                strokeWidth = dp(context, 1)
                setTextColor(NbkTheme.NAVY)
            }
            ButtonStyle.DANGER -> {
                backgroundTintList = ColorStateList.valueOf(NbkTheme.surface(context))
                strokeColor = ColorStateList.valueOf(0xFFFFB4A8.toInt())
                strokeWidth = dp(context, 1)
                setTextColor(NbkTheme.RED)
            }
        }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(context, 5)
            bottomMargin = dp(context, 5)
        }
        setOnClickListener { onClick() }
    }

    enum class ButtonStyle { PRIMARY, GOLD, OUTLINE, DANGER }

    fun input(context: Context, hint: String, inputType: Int = InputType.TYPE_CLASS_TEXT, initialValue: String = ""): EditText =
        EditText(context).apply {
            this.hint = hint
            setText(initialValue)
            this.inputType = inputType
            textSize = 15f
            setTextColor(NbkTheme.text(context))
            setHintTextColor(NbkTheme.secondaryText(context))
            setPadding(dp(context, 14), dp(context, 13), dp(context, 14), dp(context, 13))
            background = GradientDrawable().apply {
                setColor(NbkTheme.surface(context))
                cornerRadius = dp(context, 12).toFloat()
                setStroke(dp(context, 1), NbkTheme.border(context))
            }
            minHeight = dp(context, 50)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(context, 8)
            }
        }

    fun labeledInput(
        context: Context,
        parent: LinearLayout,
        label: String,
        hint: String,
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        initialValue: String = ""
    ): EditText {
        parent.addView(label(context, label))
        return input(context, hint, inputType, initialValue).also(parent::addView)
    }

    fun logo(context: Context, navyBackground: Boolean = false, height: Int = 72): ImageView = ImageView(context).apply {
        setImageResource(if (navyBackground) com.example.nbk_travels.R.drawable.nbk_logo_navy else com.example.nbk_travels.R.drawable.nbk_logo)
        scaleType = ImageView.ScaleType.FIT_CENTER
        adjustViewBounds = true
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, height))
    }

    fun topBar(context: Context, title: String, onBack: (() -> Unit)? = null): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(context, 12), dp(context, 6), dp(context, 18), dp(context, 6))
        setBackgroundResource(com.example.nbk_travels.R.drawable.bg_navy_header)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 62))
        if (onBack != null) {
            addView(TextView(context).apply {
                text = "â€¹"
                textSize = 34f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                contentDescription = "Go back"
                layoutParams = LinearLayout.LayoutParams(dp(context, 44), ViewGroup.LayoutParams.MATCH_PARENT)
                setOnClickListener { onBack() }
            })
        }
        addView(text(context, title, 17f, Color.WHITE, true), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    }

    fun bottomNav(
        context: Context,
        selected: String,
        onNavigate: (String) -> Unit
    ): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundColor(NbkTheme.surface(context))
        elevation = dp(context, 8).toFloat()
        setPadding(dp(context, 5), dp(context, 6), dp(context, 5), dp(context, 5))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 68))
        val items = listOf(
            Triple("Home", "home", com.example.nbk_travels.R.drawable.ic_home),
            Triple("Bookings", "bookings", com.example.nbk_travels.R.drawable.ic_bookings),
            Triple("Settings", "settings", com.example.nbk_travels.R.drawable.ic_settings),
            Triple("Help", "help", com.example.nbk_travels.R.drawable.ic_help)
        )
        items.forEach { (label, key, iconId) ->
            val item = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                val tint = if (key == selected) NbkTheme.BLUE else NbkTheme.secondaryText(context)
                val icon = ImageView(context).apply {
                    setImageResource(iconId)
                    DrawableCompat.setTint(DrawableCompat.wrap(drawable).mutate(), tint)
                    layoutParams = LinearLayout.LayoutParams(dp(context, 22), dp(context, 22))
                }
                addView(icon)
                addView(text(context, label, 10.5f, tint, key == selected))
                setOnClickListener { onNavigate(key) }
                background = if (key == selected) GradientDrawable().apply {
                    setColor(NbkTheme.PALE_BLUE)
                    cornerRadius = dp(context, 12).toFloat()
                } else null
            }
            addView(item, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                leftMargin = dp(context, 2); rightMargin = dp(context, 2)
            })
        }
    }

    fun withBottomNav(context: Context, body: View, selected: String, onNavigate: (String) -> Unit): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(NbkTheme.background(context))
        addView(body, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        addView(bottomNav(context, selected, onNavigate))
    }

    fun headerAction(context: Context, label: String, onClick: () -> Unit): TextView = text(context, label, 24f, Color.WHITE, true).apply {
        gravity = Gravity.CENTER
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(dp(context, 44), dp(context, 44))
    }

    fun row(context: Context, start: View, end: View? = null, onClick: (() -> Unit)? = null): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(start, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        if (end != null) addView(end)
        if (onClick != null) setOnClickListener { onClick() }
    }

    fun statusPill(context: Context, status: String): TextView = text(context, status.replaceFirstChar { it.uppercase() }, 11f, when (status.lowercase()) {
        "confirmed" -> NbkTheme.GREEN
        "completed" -> NbkTheme.BLUE
        "cancelled", "rejected" -> NbkTheme.RED
        else -> 0xFF8A5A00.toInt()
    }, true).apply {
        setPadding(dp(context, 10), dp(context, 5), dp(context, 10), dp(context, 5))
        background = GradientDrawable().apply {
            setColor(when (status.lowercase()) {
                "confirmed" -> 0xFFDDF7ED.toInt()
                "completed" -> 0xFFDDEBFF.toInt()
                "cancelled", "rejected" -> 0xFFFFE6E3.toInt()
                else -> 0xFFFFF1C6.toInt()
            })
            cornerRadius = dp(context, 15).toFloat()
        }
    }
}
