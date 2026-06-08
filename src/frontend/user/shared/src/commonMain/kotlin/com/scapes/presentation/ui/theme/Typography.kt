package com.scapes.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

val HeadingFontFamily = FontFamily.SansSerif
val BodyFontFamily = FontFamily.SansSerif

val ScapesTypography =
    Typography(
        headlineLarge =
            TextStyle(
                fontFamily = HeadingFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
                lineHeight = 1.3.em,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = HeadingFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
                lineHeight = 1.3.em,
            ),
        titleLarge =
            TextStyle(
                fontFamily = HeadingFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
                lineHeight = 1.3.em,
            ),
        titleMedium =
            TextStyle(
                fontFamily = BodyFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
                lineHeight = 1.4.em,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = BodyFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp,
                lineHeight = 1.5.em,
            ),
        labelMedium =
            TextStyle(
                fontFamily = BodyFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
                lineHeight = 1.35.em,
            ),
    )
