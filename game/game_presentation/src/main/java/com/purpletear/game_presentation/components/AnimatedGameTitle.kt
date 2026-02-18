package com.purpletear.game_presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.PlayfairDisplayFontFamily
import kotlinx.coroutines.delay


/**
 * A composable that displays animated game title and subtitle
 */
@Composable
internal fun AnimatedGameTitle(
    modifier: Modifier = Modifier,
    title: String,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // State for controlling text visibility
        val showTitle = remember { mutableStateOf(false) }
        val showSubtitle = remember { mutableStateOf(false) }

        // Trigger animations with a delay for the second text
        LaunchedEffect(key1 = true) {
            delay(300)
            showTitle.value = true
            delay(1000)
            showSubtitle.value = true
        }

        // Add text with PlayFair Display font and animation
        AnimatedVisibility(
            visible = showTitle.value,
            enter = fadeIn(animationSpec = tween(durationMillis = 800))
        ) {
            Text(
                text = title,
                fontFamily = PlayfairDisplayFontFamily,
                color = Color.White,
                fontSize = 24.sp,
            )
        }
    }
}
