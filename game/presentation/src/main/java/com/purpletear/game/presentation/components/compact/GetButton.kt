package com.purpletear.game.presentation.components.compact

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.Poppins
import com.purpletear.game.presentation.components.CircularProgress
import com.purpletear.game.presentation.states.GameState

private val ButtonShape = RoundedCornerShape(16.dp)
private val BackgroundIdle = Color(0xFF2A2A2A)
private val BackgroundError = Color(0xFFE74C3C)
private val ProgressColor = Color(0xFF4DB9EC)

@Composable
fun GetButton(
    modifier: Modifier = Modifier,
    gameState: GameState = GameState.Idle,
    onGetClick: () -> Unit = {},
    onOpenClick: () -> Unit = {},
    onCancelClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val isError = gameState == GameState.LoadingError

    val backgroundColor by animateColorAsState(
        targetValue = if (isError) BackgroundError else BackgroundIdle,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "bg_color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "press_scale"
    )

    val clickHandler = remember(gameState) {
        {
            when (gameState) {
                is GameState.DownloadingGame -> onCancelClick?.invoke() ?: Unit
                GameState.ReadyToPlay -> onOpenClick()
                GameState.LoadingError -> onGetClick()
                else -> onGetClick()
            }
        }
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(ButtonShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = clickHandler
            )
            .animateContentSize(tween(150, easing = FastOutSlowInEasing))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = gameState,
            transitionSpec = {
                fadeIn(tween(120)) + scaleIn(spring(stiffness = 350f), 0.92f) togetherWith
                fadeOut(tween(80))
            },
            label = "content"
        ) { state ->
            when (state) {
                is GameState.DownloadingGame -> {
                    if (state.progress < 100) {
                        CircularProgress(
                            progress = state.progress / 100f,
                            size = 18.dp,
                            strokeWidth = 2.dp,
                            backgroundColor = Color(0xFF3A3A3A)
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = ProgressColor,
                            strokeWidth = 2.dp,
                            trackColor = Color(0xFF3A3A3A)
                        )
                    }
                }
                else -> {
                    val text = when (state) {
                        GameState.ReadyToPlay -> "Open"
                        GameState.LoadingError -> "Retry"
                        else -> "Get"
                    }
                    ButtonLabel(text)
                }
            }
        }
    }
}

@Composable
private fun ButtonLabel(text: String) {
    Text(
        text = text,
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = Color.White
    )
}
