package com.purpletear.aiconversation.presentation.screens.home.components.header

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.purpletear.aiconversation.presentation.component.blurred_message.BlurredMessageComposable
import com.purpletear.aiconversation.presentation.component.blurred_message.MessageCornerType
import com.purpletear.core.presentation.components.video.VideoComponent
import com.purpletear.aiconversation.presentation.screens.home.components.FuturisticText
import com.purpletear.aiconversation.presentation.screens.home.viewModels.AiConversationHomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
internal fun MainHeaderComposable(
    modifier: Modifier = Modifier,
    viewModel: AiConversationHomeViewModel,
    showVideo: Boolean,
) {
    val aspectRatio = 1216 / 1664f
    val isLoading = remember { mutableStateOf(true) }

    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .then(modifier),

        contentAlignment = Alignment.TopCenter

    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .then(modifier)
        ) {
            if (showVideo) {
                VideoComponent(
                    url = "https://data.sutoko.app/resources/sutoko-ai/video/header_eva.compressed.mp4",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio),
                    onVideoPrepared = {
                        isLoading.value = false
                    }
                )
            }
        }


        // Prepare front-layer alpha (used to fade out placeholder and loading overlay)
        val alphaState by animateFloatAsState(
            targetValue = if (isLoading.value) 1f else 0f,
            animationSpec = tween(
                durationMillis = 680,
                easing = LinearOutSlowInEasing
            ), label = "Video front layer opacity animation"
        )

        // Fade this out when video is finished loaded.
        AsyncImage(
            model = "https://data.sutoko.app/resources/sutoko-ai/image/frame_ai_conversation_0_screen_.jpeg",
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .align(Alignment.TopCenter)
                .alpha(alphaState)
        )

        Column(
            modifier = Modifier
                .align(
                    Alignment.CenterEnd
                )
                .graphicsLayer(
                    translationX = -60f,
                    translationY = -100f
                )
        ) {
            FuturisticText(
                text = "Arashai",
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 26.dp, bottom = 6.dp)
                    .alpha(0.6f),
                fontSize = 14.sp
            )
            FuturisticText(
                text = viewModel.selectedCharacter.value?.firstName?.trim() ?: "",
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 26.dp, bottom = 16.dp),
                fontSize = 18.sp
            )
            // SquaresAnimation()
        }

        val colors = listOf(Color(0xFF79B4F7).copy(0f), Color(0xFF222327).copy(0.4f))
        val darkColors = listOf(Color(0xFF01050A).copy(1f), Color(0xFF01050A).copy(0f)).reversed()

        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .background(brush = Brush.verticalGradient(colors))
        )

        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(3 / 2f)
                .background(brush = Brush.verticalGradient(darkColors))
                .align(Alignment.BottomEnd)
        )

    }
}


@Composable
@Preview
private fun SquaresAnimationPreview() {
    Box(Modifier.background(Color.Black)) {
        SquaresAnimation()
    }
}

@Composable
private fun SquaresAnimation(modifier: Modifier = Modifier) {
    val mutableHeight = remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val itemWidth = 200f
    val itemHeight = 62f
    val spacing = 12f

    val mutableHeightAnimated by animateFloatAsState(
        targetValue = mutableHeight.floatValue,
        animationSpec = tween(
            durationMillis = 720,
            delayMillis = 1280,
            easing = LinearOutSlowInEasing
        ), label = "Messages animation"
    )

    LaunchedEffect(Unit) {
        repeat(3) { cycle ->
            scope.launch {
                mutableHeight.floatValue += if (mutableHeight.floatValue == 0f) {
                    itemHeight
                } else {
                    itemHeight + spacing
                }
            }
            delay(2000)
        }
    }

    Column(modifier = modifier) {
        Box(
            Modifier
                .height((itemHeight.dp) * 3 + spacing.dp * 2)
                .width(itemWidth.dp)
                .clipToBounds()
        ) {

            Box(
                Modifier
                    .height(mutableHeightAnimated.dp)
                    .heightIn(min = 0.dp, max = (itemHeight.dp) * 3 + spacing.dp * 2)
                    .clipToBounds()
                    .align(Alignment.BottomCenter),

                ) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    BlurredMessageComposable(
                        color = Color(0xFF19202E),
                        cornersType = MessageCornerType.First
                    )
                    BlurredMessageComposable(
                        color = Color(0xFF2D192E),
                        cornersType = MessageCornerType.Middle
                    )
                    BlurredMessageComposable(
                        color = Color(0xFF1D1922),
                        cornersType = MessageCornerType.Last
                    )
                }
            }
        }
    }
}


@Composable
@Preview
private fun SquaresAnimationPreview2() {
    Box(Modifier.background(Color.Black)) {
        SquaresAnimation()
    }
}


