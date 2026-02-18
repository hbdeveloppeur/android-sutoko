package com.purpletear.game_presentation.sealed

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

internal sealed class Background {
    @Keep
    data class Solid(val color: Color) : Background()
    @Keep
    data class Gradient(
        val colors: List<Color>,
        val direction: GradientDirection = GradientDirection.HORIZONTAL
    ) : Background()
}

internal enum class GradientDirection {
    HORIZONTAL,
    VERTICAL,
    TOP_LEFT_TO_BOTTOM_RIGHT,
    TOP_RIGHT_TO_BOTTOM_LEFT
}

internal fun Background.toBrush(): Brush {
    return when (this) {
        is Background.Solid -> Brush.verticalGradient(listOf(this.color, this.color))
        is Background.Gradient -> {
            when (this.direction) {
                GradientDirection.HORIZONTAL -> Brush.horizontalGradient(this.colors)
                GradientDirection.VERTICAL -> Brush.verticalGradient(this.colors)
                GradientDirection.TOP_LEFT_TO_BOTTOM_RIGHT -> Brush.linearGradient(this.colors)
                GradientDirection.TOP_RIGHT_TO_BOTTOM_LEFT -> Brush.linearGradient(this.colors)
            }
        }
    }
}
