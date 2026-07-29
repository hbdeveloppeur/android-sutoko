package com.purpletear.sutoko.shop.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.example.sharedelements.theme.Poppins

/**
 * Bold white amount that counts up/down smoothly when its numeric value changes.
 * Non-numeric values (e.g. placeholders) are rendered as-is.
 */
@Composable
fun AnimatedAmountText(
    value: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val numeric = value.toIntOrNull()
    val text = if (numeric == null) {
        value
    } else {
        val animated by animateIntAsState(
            targetValue = numeric,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            label = "AnimatedAmountText",
        )
        animated.toString()
    }
    Text(
        text = text,
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize,
        color = Color.White,
        modifier = modifier,
    )
}
