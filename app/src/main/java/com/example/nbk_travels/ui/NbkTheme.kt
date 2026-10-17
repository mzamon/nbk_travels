package com.example.nbk_travels.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

object NbkTheme {
    const val NAVY = 0xFF0B2B5B.toInt()
    const val DEEP_NAVY = 0xFF082343.toInt()
    const val BLUE = 0xFF075BB5.toInt()
    const val GOLD = 0xFFF5B719.toInt()
    const val WHITE = Color.WHITE
    const val GREEN = 0xFF087F5B.toInt()
    const val RED = 0xFFC2410C.toInt()
    const val MUTED = 0xFF64748B.toInt()
    const val BORDER = 0xFFDCE5EF.toInt()
    const val PALE_BLUE = 0xFFEAF2FC.toInt()

    fun isDark(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    fun background(context: Context): Int = if (isDark(context)) 0xFF101827.toInt() else 0xFFF4F7FB.toInt()
    fun surface(context: Context): Int = if (isDark(context)) 0xFF182337.toInt() else WHITE
    fun text(context: Context): Int = if (isDark(context)) 0xFFF5F7FB.toInt() else 0xFF172B4D.toInt()
    fun secondaryText(context: Context): Int = if (isDark(context)) 0xFFBAC6D8.toInt() else MUTED
    fun border(context: Context): Int = if (isDark(context)) 0xFF2B3B53.toInt() else BORDER
}
