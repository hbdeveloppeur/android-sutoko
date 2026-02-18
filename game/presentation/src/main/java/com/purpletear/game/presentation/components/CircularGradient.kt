package com.purpletear.game.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A composable that draws a circular gradient.
 *
 * @param modifier The modifier to be applied to the composable.
 * @param colors The list of colors to be used in the gradient. Default is [Color.Transparent, Color.Black.copy(alpha = 0.25f)].
 */
@Composable
internal fun CircularGradient(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(Color.Transparent, Color.Black.copy(alpha = 0.25f)),
) {
    Box(modifier = modifier.background(Brush.radialGradient(colors)))
}
