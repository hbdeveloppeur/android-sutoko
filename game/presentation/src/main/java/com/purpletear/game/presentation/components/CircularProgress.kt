package com.purpletear.game.presentation.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircularProgress(
    progress: Float, // 0f..1f
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    progressColor: Color = Color(0xFF007AFF),
    backgroundColor: Color = Color.LightGray.copy(alpha = 0.3f)
) {
    val animatedProgress = animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "CircularProgressAnimation"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .padding(strokeWidth / 2)
    ) {
        val diameter = size.toPx()
        val radius = diameter / 2
        val strokePx = strokeWidth.toPx()

        // Background circle
        drawArc(
            color = backgroundColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
            size = Size(diameter, diameter)
        )

        // Foreground progress arc
        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress.value,
            useCenter = false,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
            size = Size(diameter, diameter)
        )

        // Optional: A small inner circle indicator
        drawCircle(
            color = progressColor.copy(alpha = 0.2f),
            radius = radius / 4,
            center = Offset(radius, radius)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCircularProgress() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgress(progress = 0.65f)
    }
}
