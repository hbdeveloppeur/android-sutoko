package com.purpletear.game_presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.purpletear.game_presentation.components.GradientThemes

/**
 * An animated border box with a smooth gradient color transition.
 *
 * Award-winning principles:
 * - Robust align-to logic and edge-case protection
 * - Cycle seamlessly through all gradients
 * - Modern, non-deprecated Compose APIs
 * - Tweakable parameters
 *
 * @param modifier Modifier to be applied to the layout
 * @param borderWidth Width of the border
 * @param borderRadius Corner radius of the border
 * @param animationTotalDuration Total duration of one complete animation cycle in milliseconds
 * @param iteration Initial phase/iteration of the animation cycle (0f to start from beginning)
 * @param theme The gradient theme to use for the border animation (defaults to Original theme)
 */
@Composable
fun AnimatedGradientBorderBox(
    modifier: Modifier = Modifier,
    borderWidth: Dp = 3.dp,
    borderRadius: Dp = 32.dp,
    animationTotalDuration: Int = 3000, // Total cycle, not per transition
    iteration: Float = 0f, // Initial animation phase/iteration
    theme: List<List<Color>> = GradientThemes.Original
) {
    // 1. Define gradient stops: all lists are non-empty and non-singular in Color
    val gradients: List<List<Color>> = theme.filter { it.isNotEmpty() }

    val maxStops = gradients.maxOfOrNull { it.size } ?: 0

    // Safeguard: if nothing to draw, skip
    if (gradients.isEmpty() || maxStops == 0) return

    // Safer align-to logic
    fun List<Color>.alignTo(size: Int): List<Color> =
        if (isEmpty()) List(size) { Color.Transparent }
        else if (size <= this.size) this.take(size)
        else this + List(size - this.size) { this.last() }

    val alignedGradients = gradients.map { it.alignTo(maxStops) }

    // 2. Animate a normalized float (0..N), where 1 unit = 1 gradient transition
    val transition = rememberInfiniteTransition(label = "GradientInfiniteTransition")
    val gradientPhase = transition.animateFloat(
        initialValue = iteration % alignedGradients.size.toFloat(), // Start from specified iteration
        targetValue = alignedGradients.size.toFloat(), // exactly total, wrap-around handled below
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationTotalDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "AnimatedGradientPhase"
    ).value

    // 3. Calculate blending indices
    val fromIndex = gradientPhase.toInt() % alignedGradients.size
    val toIndex = (fromIndex + 1) % alignedGradients.size
    val localFraction = gradientPhase - gradientPhase.toInt()
    val colorStops: List<Color> = List(maxStops) { stop ->
        lerp(
            alignedGradients[fromIndex][stop], // Safe, always the same size
            alignedGradients[toIndex][stop],
            localFraction
        )
    }

    // 4. Dimensions
    val borderStrokePx = with(LocalDensity.current) { borderWidth.toPx() }
    val radiusPx = with(LocalDensity.current) { borderRadius.toPx() }

    // 5. Draw border
    Box(
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent, shape = RoundedCornerShape(borderRadius))
        ) {
            val rect = Rect(
                left = borderStrokePx / 2,
                top = borderStrokePx / 2,
                right = size.width - borderStrokePx / 2,
                bottom = size.height - borderStrokePx / 2
            )
            // Animate gradient angle around the box
            val sweepPercent = (gradientPhase % alignedGradients.size) / alignedGradients.size
            val angle = 360f * sweepPercent
            val radians = Math.toRadians(angle.toDouble())
            val center = rect.center
            val radiusW = rect.width / 2
            val radiusH = rect.height / 2
            val start = Offset(
                x = center.x + radiusW * kotlin.math.cos(radians).toFloat(),
                y = center.y + radiusH * kotlin.math.sin(radians).toFloat()
            )
            val end = Offset(
                x = center.x - radiusW * kotlin.math.cos(radians).toFloat(),
                y = center.y - radiusH * kotlin.math.sin(radians).toFloat()
            )

            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = colorStops,
                    start = start,
                    end = end
                ),
                topLeft = Offset(rect.left, rect.top),
                size = rect.size,
                style = Stroke(width = borderStrokePx),
                cornerRadius = CornerRadius(radiusPx)
            )
        }
        // Content area ("mask" for border)
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Transparent, shape = RoundedCornerShape(borderRadius))
        )
    }
}
