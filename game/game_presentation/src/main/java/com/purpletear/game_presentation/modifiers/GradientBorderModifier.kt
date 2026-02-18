package com.purpletear.game_presentation.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Extension function to add a gradient border to a composable.
 *
 * @param brush The brush to use for the border.
 * @param width The width of the border.
 * @param cornerRadius The corner radius of the border.
 */
fun Modifier.gradientBorder(
    brush: Brush,
    width: Dp = 1.dp,
    cornerRadius: Dp = 0.dp
) = this.drawWithContent {
    drawContent()
    
    val strokeWidth = width.toPx()
    val radius = cornerRadius.toPx()
    
    drawRoundRect(
        brush = brush,
        topLeft = Offset(0f, 0f),
        size = Size(size.width, size.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
        style = Stroke(width = strokeWidth)
    )
}