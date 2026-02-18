package com.purpletear.game_presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import com.purpletear.game_presentation.sealed.Background
import com.purpletear.game_presentation.sealed.GradientDirection
import com.purpletear.game_presentation.sealed.toBrush

@Composable
internal fun VerticalGradient(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .alpha(0.3f)
            .background(
                Background.Gradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    direction = GradientDirection.VERTICAL
                ).toBrush()
            )
            .fillMaxWidth()
            .then(modifier)
    )
}