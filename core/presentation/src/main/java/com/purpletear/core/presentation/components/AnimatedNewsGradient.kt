package com.purpletear.core.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedNewsGradient(modifier: Modifier = Modifier, alpha: Float = 0.12f) {
    // Animate a normalized 0..1 progress that sweeps a soft diagonal band
    val infinite = rememberInfiniteTransition(label = "newsGradient")
    val progress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = FastOutSlowInEasing),
        ),
        label = "newsGradientProgress"
    )

    var widthPx by remember { mutableStateOf(0f) }
    var heightPx by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier.onSizeChanged { size ->
            widthPx = size.width.toFloat()
            heightPx = size.height.toFloat()
        }
    ) {
        // Diagonal shimmering band as a background brush
        val band = 0.14f // soft band width
        val center = progress.coerceIn(0f, 1f)
        val a = (center - band).coerceIn(0f, 1f)
        val b = center
        val c = (center + band).coerceIn(0f, 1f)

        val start = androidx.compose.ui.geometry.Offset(0f, heightPx)
        val end = androidx.compose.ui.geometry.Offset(widthPx * 1.3f + band, 0f)

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            a to Color.Transparent,
                            b to Color.White.copy(alpha = alpha),
                            c to Color.Transparent,
                            1f to Color.Transparent,
                        ),
                        start = start,
                        end = end
                    )
                )
        )

        // Static black fade at the bottom to ensure text legibility
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black
                        )
                    )
                )
        )
    }
}
