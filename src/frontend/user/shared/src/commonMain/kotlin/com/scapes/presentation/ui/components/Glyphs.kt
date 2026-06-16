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
