package com.scapes.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.model.WallpaperSource
import com.scapes.presentation.ui.theme.ScapesThemeColors

data class WallpaperUi(
    val wallpaper: Wallpaper,
    val title: String,
    val author: String,
    val height: Dp,
    val resolution: String,
    val colors: List<Color>,
    val rearHorizon: Float,
    val frontHorizon: Float,
    val imageUrl: String? = null,
)

@Composable
fun WallpaperVisual(
    wallpaper: WallpaperUi,
    colors: ScapesThemeColors,
    modifier: Modifier = Modifier,
    imageUrl: String? = wallpaper.imageUrl,
) {
    Box(modifier = modifier) {
        NeutralWallpaperFallback(colors = colors, modifier = Modifier.fillMaxSize())
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun NeutralWallpaperFallback(colors: ScapesThemeColors, modifier: Modifier = Modifier) {
    NeutralPlaceholderSurface(colors = colors, modifier = modifier)
}

@Composable
internal fun NeutralPlaceholderSurface(
    colors: ScapesThemeColors,
    modifier: Modifier = Modifier,
) {
    val shimmer = rememberInfiniteTransition(label = "neutral-placeholder")
    val progress by
        shimmer.animateFloat(
            initialValue = -1.2f,
            targetValue = 2.2f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 1350, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "neutral-placeholder-progress",
        )

    Box(
        modifier =
            modifier.background(
                Brush.linearGradient(
                    colors = neutralPlaceholderPalette(colors),
                    start = Offset(progress * 320f, 0f),
                    end = Offset((progress + 1f) * 320f, 360f),
                )
            )
    )
}

private fun neutralPlaceholderPalette(colors: ScapesThemeColors): List<Color> =
    if (colors.text == Color.Black) {
        listOf(Color(0xFFBFC5CD), Color(0xFFDCE1E7), Color(0xFFBFC5CD))
    } else {
        listOf(Color(0xFF4E5560), Color(0xFF676F7A), Color(0xFF4E5560))
    }

fun Wallpaper.toUi(index: Int): WallpaperUi {
    val palette = source.toWallpaperPalette()
    val displayTitle = title.replaceFirstChar { char -> char.uppercase() }

    return WallpaperUi(
        wallpaper = this,
        title = displayTitle,
        author =
            authorName ?: source.name.lowercase().replaceFirstChar { char -> char.uppercase() },
        height = listOf(248.dp, 292.dp, 226.dp, 318.dp, 270.dp)[index % 5],
        resolution =
            if (width > 0 && height > 0) {
                "${width}x${height}"
            } else {
                "Portrait"
            },
        colors = palette,
        rearHorizon = 0.54f + (index % 4) * 0.04f,
        frontHorizon = 0.74f + (index % 3) * 0.03f,
        imageUrl = previewUrl,
    )
}

fun WallpaperSource.toWallpaperPalette(): List<Color> =
    when (this) {
        WallpaperSource.PEXELS -> listOf(Color(0xFF0D6271), Color(0xFFF9C52E), Color(0xFF137586))

        WallpaperSource.UNSPLASH -> listOf(Color(0xFF70C3C6), Color(0xFFF8F8EF), Color(0xFF0F0F0F))

        WallpaperSource.PIXABAY -> listOf(Color(0xFF1A6D75), Color(0xFF70C3C6), Color(0xFF0D6271))

        WallpaperSource.SCAPES_API ->
            listOf(Color(0xFF0F0F0F), Color(0xFFF8F8EF), Color(0xFF202828))
    }
