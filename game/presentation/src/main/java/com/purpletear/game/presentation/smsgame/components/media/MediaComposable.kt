package com.purpletear.game.presentation.smsgame.components.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun MediaComposable(imageUrl: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0920))
    ) {
        // Placeholder for background image - remove transparency overlay when implemented
        if (imageUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }
    }
}