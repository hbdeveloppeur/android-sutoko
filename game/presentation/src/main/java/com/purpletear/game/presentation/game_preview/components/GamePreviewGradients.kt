package com.purpletear.game.presentation.game_preview.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.game_preview.PositionedCircularGradient
import com.purpletear.game.presentation.game_preview.VerticalGradient

internal val PremiumLabelGradient = listOf(
    Color(0xFFFECF00),
    Color(0xFFFF7FDF),
    Color(0xFF5D8BFF),
)

internal val PremiumActiveLabelGradient = listOf(
    Color(0xFFFECF00),
    Color(0xFFFF7FDF),
    Color(0xFF3B30E7),
)

internal val UnlockedLabelGradient = listOf(
    Color(0xFF3D753A),
    Color(0xFF51FF40),
    Color(0xFF50A44D),
)

private const val LEFT_TRANSLATION_FACTOR = -2f
private const val RIGHT_TRANSLATION_FACTOR = 2f

@Composable
internal fun GamePreviewGradients(
    screenWidth: Int,
    screenHeight: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        PositionedCircularGradient(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            translationXFactor = LEFT_TRANSLATION_FACTOR,
            alpha = 0.2f
        )

        PositionedCircularGradient(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            translationXFactor = RIGHT_TRANSLATION_FACTOR,
            alpha = 0.1f
        )

        VerticalGradient(
            modifier = Modifier
                .height(160.dp)
                .align(Alignment.TopCenter)
        )
    }
}
