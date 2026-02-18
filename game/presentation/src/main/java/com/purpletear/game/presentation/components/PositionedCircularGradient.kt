package com.purpletear.game.presentation.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * A wrapper for CircularGradient that positions it with specific translation, scale, and alpha values.
 *
 * @param screenWidth The width of the screen in dp.
 * @param screenHeight The height of the screen in dp.
 * @param translationXFactor The factor to multiply screenWidth by for X translation. Positive values move right, negative move left.
 * @param alpha The opacity of the gradient.
 * @param colors The list of colors to be used in the gradient.
 */
@Composable
internal fun PositionedCircularGradient(
    screenWidth: Int,
    screenHeight: Int,
    translationXFactor: Float,
    alpha: Float = 0.2f,
    colors: List<Color> = listOf(Color(0xFF000000), Color.Transparent)
) {
    // Calculate size based on screen dimensions
    val gradientSize = (screenWidth * 1.5f).dp
    
    CircularGradient(
        Modifier
            .width(gradientSize)
            .height(gradientSize)
            .graphicsLayer {
                // Calculate translation values based on screen dimensions
                translationX = screenWidth.toFloat() * translationXFactor
                translationY = screenHeight.toFloat()
                this.alpha = alpha
                scaleY = 2.5f
            },
        colors = colors
    )
}