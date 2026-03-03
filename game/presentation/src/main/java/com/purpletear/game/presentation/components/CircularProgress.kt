package com.purpletear.game.presentation.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircularProgress(
    progress: Float, // 0f..1f
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
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
        val strokePx = strokeWidth.toPx()
        val diameter = size.toPx() - strokePx

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
    }
}