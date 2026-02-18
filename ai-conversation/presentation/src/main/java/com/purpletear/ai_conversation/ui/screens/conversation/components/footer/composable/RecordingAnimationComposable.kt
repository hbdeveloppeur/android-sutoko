package com.purpletear.ai_conversation.ui.screens.conversation.components.footer.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.component.textarea.MultiLineTextInput
import com.purpletear.ai_conversation.ui.theme.AiConversationTheme


@Composable
@Preview(name = "RecordingAnimationComposable", showBackground = false, showSystemUi = false)
private fun Preview() {

    val verticalRules = listOf(14.dp)
    val rulesEnabled = false
    AiConversationTheme {
        Box {
            Column(
                Modifier
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.preview_tool_button),
                    contentDescription = null,
                )
                Box(
                    Modifier
                        .height(48.dp)
                        .padding(vertical = 12.dp)
                ) {
                    RecordingAnimationComposable(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                    )
                }
            }
            if (rulesEnabled) {
                verticalRules.forEach { startPadding ->
                    Box(
                        Modifier
                            .padding(start = startPadding)
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(Color.Red)
                    )
                }
            }
        }
    }
}

@Composable
internal fun RecordingAnimationComposable(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val shape = RoundedCornerShape(50)
    val hasFocus = remember { mutableStateOf(false) }
    Box(
        modifier = modifier
    ) {
        MultiLineTextInput(
            modifier = Modifier
                .border(.5.dp, Color.White.copy(0.1f), shape)
                .background(Color.Black, shape)
                .clip(shape),
            text = "",
            textStyle = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = Color.White
            ),
            onChange = { _ -> },
            onFocused = {
                hasFocus.value = it
            },
            placeholder = "",
            enabled = false,
        )
        RecordingLottieAnimation(Modifier.align(Alignment.Center), isLoading = isLoading)
    }
}


@Composable
private fun RecordingLottieAnimation(modifier: Modifier = Modifier, isLoading: Boolean = true) {
    val rawRes = R.raw.lottie_recording
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawRes))
    Box(
        modifier = Modifier
            .size(46.dp)
            .clipToBounds()
            .then(modifier),
        contentAlignment = Alignment.Center

    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.Center),
                color = Color.LightGray,
                strokeWidth = 2.dp
            )
        } else {
            LottieAnimation(
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = 1.6f
                        scaleY = 1.6f
                    },
                composition = composition,
                iterations = LottieConstants.IterateForever,
            )
        }
    }
}