package com.scapes.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
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
) {
    Box(modifier = modifier) {
        WallpaperArt(wallpaper = wallpaper, colors = colors, modifier = Modifier.fillMaxSize())
        if (wallpaper.imageUrl != null) {
            AsyncImage(
                model = wallpaper.imageUrl,
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun WallpaperArt(wallpaper: WallpaperUi, colors: ScapesThemeColors, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(wallpaper.colors.first())) {
        drawRect(brush = Brush.verticalGradient(wallpaper.colors), size = size)
        drawPath(
            path =
                Path().apply {
                    moveTo(size.width * 0.54f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height * 0.44f)
                    close()
                },
            color = colors.amber.copy(alpha = 0.18f),
        )
        val sunRadius = size.minDimension * 0.16f
        drawCircle(
            color = colors.amber.copy(alpha = 0.28f),
            radius = sunRadius,
            center = Offset(size.width * 0.72f, size.height * 0.18f),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.14f),
            radius = sunRadius * 1.7f,
            center = Offset(size.width * 0.72f, size.height * 0.18f),
            style = Stroke(width = 3.dp.toPx()),
        )
        val rear =
            Path().apply {
                moveTo(0f, size.height * wallpaper.rearHorizon)
                lineTo(size.width * 0.28f, size.height * (wallpaper.rearHorizon - 0.18f))
                lineTo(size.width * 0.58f, size.height * (wallpaper.rearHorizon - 0.04f))
                lineTo(size.width, size.height * (wallpaper.rearHorizon - 0.22f))
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
        drawPath(rear, Color.White.copy(alpha = 0.16f))
        val front =
            Path().apply {
                moveTo(0f, size.height * wallpaper.frontHorizon)
                lineTo(size.width * 0.2f, size.height * (wallpaper.frontHorizon - 0.12f))
                lineTo(size.width * 0.47f, size.height * (wallpaper.frontHorizon - 0.02f))
                lineTo(size.width * 0.75f, size.height * (wallpaper.frontHorizon - 0.19f))
                lineTo(size.width, size.height * (wallpaper.frontHorizon - 0.08f))
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
        drawPath(front, colors.text.copy(alpha = 0.22f))
        drawLine(
            color = Color.White.copy(alpha = 0.22f),
            start = Offset(size.width * 0.12f, size.height * 0.08f),
            end = Offset(size.width * 0.48f, size.height * 0.08f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
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
