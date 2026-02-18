package com.purpletear.ai_conversation.ui.screens.conversation.components.footer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.component.textarea.MultiLineTextInput
import com.purpletear.ai_conversation.ui.icons.SendMessageIconComposable
import com.purpletear.core.presentation.services.performVibration
import com.purpletear.ai_conversation.ui.screens.conversation.components.footer.composable.RecordButton
import com.purpletear.ai_conversation.ui.screens.conversation.components.footer.composable.RecordingAnimationComposable
import com.purpletear.ai_conversation.ui.screens.conversation.viewmodels.ConversationViewModel
import com.purpletear.ai_conversation.ui.screens.conversation.viewmodels.VoiceRecordViewModel
import com.purpletear.ai_conversation.ui.screens.conversation.viewmodels.states.RecordingState

@Composable
internal fun ConversationFooter(
    modifier: Modifier = Modifier,
    viewModel: ConversationViewModel,
    voiceRecordViewModel: VoiceRecordViewModel,
) {
    val isBlocked = viewModel.conversationSettings.value?.isBlocked ?: false
    val buttonEnabled = !isBlocked && !voiceRecordViewModel.isRecording.value
    val isDeletable = remember { mutableStateOf(false) }
    val alphaAnimation by animateFloatAsState(
        targetValue = if (buttonEnabled) 1f else 0f,
        animationSpec = tween(
            durationMillis = 280,
            easing = LinearOutSlowInEasing
        ), label = "mutableHeightAnimated"
    )

    val context = LocalContext.current
    LaunchedEffect(isDeletable.value, voiceRecordViewModel.isRecording.value) {
        if (isDeletable.value || voiceRecordViewModel.isRecording.value) {
            performVibration(context, 120L)
        }
    }

    Box(
        modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .alpha(if (isBlocked) 0.2f else 1f),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Button - tools
            Box {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isBlocked && !voiceRecordViewModel.isRecording.value,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    ButtonTools(
                        Modifier
                            .padding(start = 12.dp),
                        enabled = !isBlocked && !voiceRecordViewModel.isRecording.value,
                        onClick = viewModel::onClickToolsViewButton
                    ) {
                        if (viewModel.toolsViewIsOpened.value) {
                            ArrowDown()
                        } else {
                            SquareAnimation()
                        }
                    }
                }


                androidx.compose.animation.AnimatedVisibility(
                    visible = !isBlocked && voiceRecordViewModel.isRecording.value,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    ButtonTools(
                        Modifier
                            .padding(start = 12.dp),
                        enabled = true,
                        onClick = {
                            isDeletable.value = false
                            voiceRecordViewModel.onRecordingAction(
                                RecordingState.StopRecording(
                                    isCanceled = true
                                )
                            )
                            performVibration(context, 100L)
                        }
                    ) {
                        DeleteIcon(color = if (isDeletable.value) Color.Red else Color.White)
                    }
                }
            }

            Box(
                Modifier
                    .padding(start = 6.dp)
                    .weight(1f)
                    .height(44.dp)
            ) {
                // MessageEditText
                MessageEditText(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    viewModel
                )

                if (voiceRecordViewModel.isRecording.value) {
                    RecordingAnimationComposable(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        isLoading = (voiceRecordViewModel.isRecording.value && voiceRecordViewModel.counter.value < 2)
                    )
                }
            }

            // send
            ButtonTools(
                Modifier
                    .padding(start = 6.dp)
                    .alpha(if (viewModel.editTextMessage.value.isBlank()) 0.2f else alphaAnimation),
                onClick = viewModel::onClickSendButton
            ) {
                SendMessageIconComposable()
            }

            RecordButton(
                modifier = Modifier
                    .padding(start = 6.dp),
                onPress = {
                    isDeletable.value = false
                    voiceRecordViewModel.onRecordingAction(RecordingState.StartRecording)
                },
                onRelease = { isCanceled ->
                    voiceRecordViewModel.onRecordingAction(RecordingState.StopRecording(isCanceled))
                    isDeletable.value = false
                },
                onDrag = { percent ->
                    isDeletable.value = percent > 80f
                }
            )
        }

        if (isBlocked || viewModel.isLoading.value) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {}
            )
        }
    }
}

@Composable
private fun ArrowDown() {
    Icon(
        modifier = Modifier
            .size(16.dp),
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_down),
        contentDescription = "icon send a message",
        tint = Color.White
    )
}

@Composable
private fun DeleteIcon(color: Color = Color.White) {
    Icon(
        modifier = Modifier
            .size(16.dp),
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_delete),
        contentDescription = "Delete the voice message",
        tint = color
    )
}

@Composable
internal fun SquareAnimation() {
    val rawRes = R.raw.lottie_anim_square_futuristic
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawRes))

    LottieAnimation(
        modifier = Modifier
            .size(32.dp)
            .graphicsLayer {
                scaleX = 2.0f
                scaleY = 2.0f
            },
        composition = composition,
        clipSpec = LottieClipSpec.Frame(
            min = 0,
            max = 38,
            maxInclusive = true
        )
    )
}

@Composable
private fun MessageEditText(modifier: Modifier = Modifier, viewModel: ConversationViewModel) {
    val targetSize = if (viewModel.editTextMessage.value.startsWith("*")) 12 else 50
    val animatedSize by animateIntAsState(
        targetValue = targetSize,
        animationSpec = tween(
            durationMillis = 280,
            easing = FastOutSlowInEasing
        ),
        label = "Animates the shape"
    )
    val targetColor =
        if (viewModel.editTextMessage.value.startsWith("*")) Color(
            0xFF0A0C0F
        ).copy(0.15f) else Color(0xFFBED3FF).copy(0.05f)
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(
            durationMillis = 280,
            easing = FastOutSlowInEasing
        ),
        label = "Animates the color"
    )

    val shape = RoundedCornerShape(animatedSize)
    val hasFocus = remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        MultiLineTextInput(
            modifier = Modifier
                .border(.5.dp, Color.White.copy(0.1f), shape)
                .background(animatedColor, shape)
                .clip(shape),
            text = viewModel.editTextMessage.value,
            textStyle = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = Color.White
            ),
            onChange = viewModel::onMessageEditTextChanged,
            onFocused = {
                hasFocus.value = it
            },
            placeholder = stringResource(R.string.ai_conversation_edit_text_placeholder_message),
        )

        if (!hasFocus.value) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),

                verticalAlignment = Alignment.CenterVertically
            ) {

                viewModel.userCoinsCount.value?.let { tokensCount ->
                    Text(
                        text = tokensCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Image(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(18.dp)
                            .clip(CircleShape),
                        painter = painterResource(id = R.drawable.message_coin),
                        contentDescription = null,
                    )
                }
            }
        }
    }
}


@Composable
private fun ButtonTools(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {

    Box(
        modifier = modifier
            .size(32.dp)
            .clipToBounds()
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content,
    )
}
