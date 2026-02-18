package com.purpletear.ai_conversation.ui.screens.conversation.components.conversation_background

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.ai_conversation.ui.common.utils.getRemoteAssetsUrl
import com.purpletear.ai_conversation.ui.screens.conversation.viewmodels.ConversationViewModel
import kotlinx.coroutines.delay

// Define brushes outside the composable to avoid recreation on each recomposition
val brush = Brush.verticalGradient(colors = listOf(Color(0xFF05070C), Color(0xFF0E1116)))
val alphaBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF05070C).copy(0.8f),
        Color(0xFF0E1116).copy(0.8f)
    )
)

@Composable
fun ConversationBackgroundComposable(viewModel: ConversationViewModel) {
    Box(
        Modifier
            .background(brush)
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.04f),
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://data.sutoko.app/resources/sutoko-ai/image/conversation_bg.jpg")
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(viewModel.conversationSettings.value?.startingBackgroundUrl) {
            visible = false
            delay(1200L)
            visible = viewModel.conversationSettings.value?.startingBackgroundUrl != null
        }

        AnimatedVisibility(
            modifier = Modifier
                .fillMaxSize(),
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 3000)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500))
        ) {
            Box {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize(),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            getRemoteAssetsUrl(
                                viewModel.conversationSettings.value?.startingBackgroundUrl ?: ""
                            )
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(alphaBrush)
                ) {}
            }
        }
    }
}