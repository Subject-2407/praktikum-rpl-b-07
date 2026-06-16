package com.scapes.presentation.ui.theme

import androidx.compose.ui.graphics.Color

/** Scapes color palette from AGENTS.md. */
object ScapesColors {
    val LightBase = Color(0xFFF8F8EF)
    val LightDominant = Color(0xFF137586)
    val LightSecondary = Color(0xFF0D6271)
    val LightSupport = Color(0xFF70C3C6)
    val LightAmber = Color(0xFFF9C52E)

    val DarkBase = Color(0xFF0F0F0F)
    val DarkDominant = Color(0xFF2BB4C1)
    val DarkSecondary = Color(0xFF1FA6B3)
    val DarkSupport = Color(0xFF1A6D75)
    val DarkAmber = Color(0xFFFFC107)
}

data class ScapesThemeColors(
    val base: Color,
    val text: Color,
    val secondaryText: Color,
    val support: Color,
    val amber: Color,
    val surface: Color,
    val elevated: Color,
)

fun scapesThemeColors(isDark: Boolean): ScapesThemeColors =
    if (isDark) {
        ScapesThemeColors(
            base = ScapesColors.DarkBase,
            text = Color.White,
            secondaryText = Color.White.copy(alpha = 0.72f),
            support = ScapesColors.DarkSupport,
            amber = ScapesColors.DarkAmber,
            surface = Color(0xFF171B1B),
            elevated = Color(0xFF202828),
        )
    } else {
        ScapesThemeColors(
            base = ScapesColors.LightBase,
            text = Color.Black,
            secondaryText = Color.Black.copy(alpha = 0.68f),
            support = ScapesColors.LightSupport,
            amber = ScapesColors.LightAmber,
            surface = Color.White,
            elevated = Color(0xFFE9F4F2),
        )
    }
