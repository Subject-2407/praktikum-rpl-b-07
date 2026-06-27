package com.scapes.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon

@Composable
fun MenuGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(22.dp)) {
        val stroke = 2.dp.toPx()
        drawLine(
            color,
            Offset(3.dp.toPx(), 6.dp.toPx()),
            Offset(size.width - 3.dp.toPx(), 6.dp.toPx()),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            color,
            Offset(3.dp.toPx(), size.height / 2),
            Offset(size.width - 3.dp.toPx(), size.height / 2),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            color,
            Offset(3.dp.toPx(), size.height - 6.dp.toPx()),
            Offset(size.width - 8.dp.toPx(), size.height - 6.dp.toPx()),
            stroke,
            StrokeCap.Round,
        )
    }
}

@Composable
fun SearchGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(23.dp)) {
        drawCircle(
            color = color,
            radius = size.minDimension * 0.28f,
            center = Offset(size.width * 0.42f, size.height * 0.4f),
            style = Stroke(width = 2.2.dp.toPx()),
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.62f, size.height * 0.62f),
            end = Offset(size.width * 0.86f, size.height * 0.86f),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun LoadingGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        drawArc(
            color = color.copy(alpha = 0.3f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 120f,
            useCenter = false,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
fun ChevronDownGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(18.dp)) {
        val stroke = 2.dp.toPx()
        drawLine(
            color,
            Offset(size.width * 0.24f, size.height * 0.38f),
            Offset(size.width * 0.5f, size.height * 0.64f),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            color,
            Offset(size.width * 0.5f, size.height * 0.64f),
            Offset(size.width * 0.76f, size.height * 0.38f),
            stroke,
            StrokeCap.Round,
        )
    }
}

@Composable
fun BackGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(23.dp)) {
        val stroke = 2.3.dp.toPx()
        drawLine(
            color,
            Offset(size.width * 0.72f, size.height * 0.16f),
            Offset(size.width * 0.28f, size.height / 2),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            color,
            Offset(size.width * 0.28f, size.height / 2),
            Offset(size.width * 0.72f, size.height * 0.84f),
            stroke,
            StrokeCap.Round,
        )
    }
}

@Composable
fun CloseGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(22.dp)) {
        drawLine(
            color,
            Offset(4.dp.toPx(), 4.dp.toPx()),
            Offset(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()),
            2.3.dp.toPx(),
            StrokeCap.Round,
        )
        drawLine(
            color,
            Offset(size.width - 4.dp.toPx(), 4.dp.toPx()),
            Offset(4.dp.toPx(), size.height - 4.dp.toPx()),
            2.3.dp.toPx(),
            StrokeCap.Round,
        )
    }
}

@Composable
fun DownloadGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(22.dp)) {
        val stroke = 2.1.dp.toPx()
        val centerX = size.width / 2
        drawLine(
            color,
            Offset(centerX, 4.dp.toPx()),
            Offset(centerX, size.height * 0.62f),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            color,
            Offset(centerX, size.height * 0.62f),
            Offset(size.width * 0.34f, size.height * 0.46f),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            color,
            Offset(centerX, size.height * 0.62f),
            Offset(size.width * 0.66f, size.height * 0.46f),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            color,
            Offset(4.dp.toPx(), size.height - 5.dp.toPx()),
            Offset(size.width - 4.dp.toPx(), size.height - 5.dp.toPx()),
            stroke,
            StrokeCap.Round,
        )
    }
}

@Composable
fun ResolutionGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(18.dp)) {
        val stroke = 1.8.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(2.dp.toPx(), 3.dp.toPx()),
            size =
                androidx.compose.ui.geometry.Size(
                    width = size.width - 4.dp.toPx(),
                    height = size.height - 6.dp.toPx(),
                ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = stroke),
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.3f, size.height * 0.68f),
            end = Offset(size.width * 0.46f, size.height * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.46f, size.height * 0.5f),
            end = Offset(size.width * 0.68f, size.height * 0.32f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun ApplyGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(22.dp)) {
        val stroke = 1.9.dp.toPx()
        val corner = 4.dp.toPx()

        drawRoundRect(
            color = color,
            topLeft = Offset(3.dp.toPx(), 4.dp.toPx()),
            size =
                androidx.compose.ui.geometry.Size(
                    width = size.width - 6.dp.toPx(),
                    height = size.height * 0.52f,
                ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.38f, size.height - 5.dp.toPx()),
            end = Offset(size.width * 0.62f, size.height - 5.dp.toPx()),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width / 2f, size.height * 0.58f),
            end = Offset(size.width / 2f, size.height * 0.76f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width / 2f, size.height * 0.76f),
            end = Offset(size.width * 0.42f, size.height * 0.68f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width / 2f, size.height * 0.76f),
            end = Offset(size.width * 0.58f, size.height * 0.68f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun MoonGlyph(color: Color, cutoutColor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        drawCircle(
            color.copy(alpha = 0.18f),
            radius = size.minDimension * 0.48f,
            center = Offset(size.width / 2, size.height / 2),
        )
        drawCircle(
            color,
            radius = size.minDimension * 0.32f,
            center = Offset(size.width * 0.48f, size.height * 0.45f),
        )
        drawCircle(
            cutoutColor,
            radius = size.minDimension * 0.28f,
            center = Offset(size.width * 0.6f, size.height * 0.32f),
        )
    }
}

@Composable
fun SunGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        drawCircle(
            color,
            radius = size.minDimension * 0.22f,
            center = Offset(size.width / 2, size.height / 2),
        )
        val stroke = 2.dp.toPx()
        listOf(
                Offset(size.width / 2, 1.dp.toPx()) to Offset(size.width / 2, 6.dp.toPx()),
                Offset(size.width / 2, size.height - 1.dp.toPx()) to
                    Offset(size.width / 2, size.height - 6.dp.toPx()),
                Offset(1.dp.toPx(), size.height / 2) to Offset(6.dp.toPx(), size.height / 2),
                Offset(size.width - 1.dp.toPx(), size.height / 2) to
                    Offset(size.width - 6.dp.toPx(), size.height / 2),
            )
            .forEach { (start, end) -> drawLine(color, start, end, stroke, StrokeCap.Round) }
    }
}
@Composable
fun SettingsGlyph(color: Color, modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Outlined.Settings,
        contentDescription = "Settings",
        tint = color,
        modifier = modifier.size(18.dp)
    )
}

@Composable
fun FolderGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(18.dp)) {
        val stroke = 1.7.dp.toPx()
        val left = 2.dp.toPx()
        val right = size.width - 2.dp.toPx()
        val top = 4.dp.toPx()
        val bottom = size.height - 3.dp.toPx()
        val tabWidth = size.width * 0.38f
        val tabTop = 2.dp.toPx()
        val tabBottom = top
        val corner = 2.dp.toPx()
        // Folder tab
        drawRoundRect(
            color = color,
            topLeft = Offset(left, tabTop),
            size = androidx.compose.ui.geometry.Size(tabWidth, tabBottom - tabTop + corner),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            style = Stroke(width = stroke),
        )
        // Main folder body
        drawRoundRect(
            color = color,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            style = Stroke(width = stroke),
        )
    }
}

@Composable
fun KeyGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(18.dp)) {
        val stroke = 1.7.dp.toPx()
        // Key head (circle)
        drawCircle(
            color = color,
            radius = size.minDimension * 0.22f,
            center = Offset(size.width * 0.34f, size.height * 0.36f),
            style = Stroke(width = stroke),
        )
        // Key shaft
        drawLine(
            color = color,
            start = Offset(size.width * 0.48f, size.height * 0.5f),
            end = Offset(size.width * 0.82f, size.height * 0.82f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        // Key teeth
        drawLine(
            color = color,
            start = Offset(size.width * 0.68f, size.height * 0.68f),
            end = Offset(size.width * 0.78f, size.height * 0.58f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}
