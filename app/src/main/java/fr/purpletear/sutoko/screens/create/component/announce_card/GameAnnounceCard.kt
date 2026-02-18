package fr.purpletear.sutoko.screens.create.component.announce_card

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sharedelements.theme.Poppins
import com.purpletear.aiconversation.presentation.theme.PinkColor
import com.purpletear.core.presentation.components.video.VideoComponent
import com.purpletear.game.presentation.viewmodels.GameAnnounceCardViewModel
import com.purpletear.sutoko.core.domain.date.RelativeDateFormatter
import com.purpletear.version.presentation.R
import fr.purpletear.sutoko.BuildConfig

@Composable
internal fun GameAnnounceCard(
    modifier: Modifier = Modifier,
    viewModel: GameAnnounceCardViewModel = hiltViewModel()
) {
    val shape = RoundedCornerShape(8.dp)

    // Fetch the release by version name when the card appears
    LaunchedEffect(BuildConfig.VERSION_NAME) {
        viewModel.loadReleaseByName("Your Turn")
    }

    Box(
        modifier = modifier
            .widthIn(min = 270.dp, max = 500.dp)
            .fillMaxSize(0.85f)
            .aspectRatio(36f / 48f)
            .border(1.dp, Color(0xFF1A1A1A), shape)
            .clip(shape)
    ) {
        BackgroundMedia(
            Modifier
                .matchParentSize()
                .clip(shape)
        )

        // Pre-capture back dispatcher to avoid composable reads inside click lambda
        val backDispatcher =
            androidx.activity.compose.LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

        // Close button (top-left) with low opacity
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(28.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    backDispatcher?.onBackPressed()
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✕",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Column(
            modifier = Modifier
                .matchParentSize()
                .padding(vertical = 32.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CardDate(Modifier, viewModel.releaseDate)
            val isTodayOrEarlier = RelativeDateFormatter.isTodayOrEarlier(viewModel.releaseDate)
            Text(
                text = if (isTodayOrEarlier) stringResource(id = R.string.game_announce_update_today) else stringResource(
                    id = R.string.game_announce_soon_available
                ),
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                color = Color.White,
                fontSize = 11.5.sp
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = stringResource(id = R.string.game_announce_title_create_stories),
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = PinkColor,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(id = R.string.game_announce_subtitle_features),
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFEAEAEA),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )

            val reminderEnabled = viewModel.reminderEnabled
            val buttonText = if (isTodayOrEarlier) {
                stringResource(id = R.string.game_announce_button_update)
            } else {
                if (reminderEnabled) "Rappel activé" else stringResource(id = R.string.game_announce_button_remind_me)
            }
            val buttonAlphaTarget = if (!isTodayOrEarlier && reminderEnabled) 0.5f else 1f
            val buttonAlpha by animateFloatAsState(
                targetValue = buttonAlphaTarget,
                animationSpec = tween(durationMillis = 200),
                label = "reminder_button_alpha"
            )
            val buttonBgTarget =
                if (!isTodayOrEarlier && reminderEnabled) Color(0xFFE3FDE9) else Color(
                    0xFFE2E3E2
                )
            val buttonBgColor by androidx.compose.animation.animateColorAsState(
                targetValue = buttonBgTarget,
                animationSpec = tween(durationMillis = 200),
                label = "reminder_button_bg_color"
            )
            AnimatedContent(
                targetState = buttonText,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                label = "reminder_button_text_switch"
            ) { animatedText ->
                ButtonText(
                    Modifier
                        .padding(top = 16.dp)
                        .alpha(buttonAlpha),
                    text = animatedText,
                    backgroundColor = buttonBgColor
                ) {
                    if (!isTodayOrEarlier) {
                        viewModel.toggleReminder()
                    }
                }
            }
        }

        val overlayTargetAlpha = if (viewModel.isLoading) 1f else 0f
        val overlayAlpha by animateFloatAsState(
            targetValue = overlayTargetAlpha,
            animationSpec = tween(durationMillis = 300),
            label = "loading_overlay_fade"
        )
        val showLoadingOverlay = overlayAlpha > 0.01f
        if (showLoadingOverlay) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(overlayAlpha)
                    .background(Color.Black.copy(alpha = 1f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = PinkColor,
                    strokeWidth = 2.5.dp
                )
            }
        }
    }
}

@Composable
private fun BackgroundMedia(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.BottomCenter) {
        // Delay video initialization until after the first composition frame is fully drawn
        // to avoid jank and blocked frames during initial layout.
        val canStartVideo = remember { androidx.compose.runtime.mutableStateOf(false) }
        val videoPrepared = remember { androidx.compose.runtime.mutableStateOf(false) }
        val videoAlpha by animateFloatAsState(
            targetValue = if (videoPrepared.value) 0f else 1f,
            animationSpec = tween(durationMillis = 450),
            label = "video_fade_in"
        )
        LaunchedEffect(Unit) {
            // Wait for the next frame to ensure composable is laid out and drawn at least once
            withFrameNanos { }
            canStartVideo.value = true
        }

        if (canStartVideo.value) {
            VideoComponent(
                url = "https://data.sutoko.app/resources/sutoko-ai/video/anounce_your_turn.optimized.mp4",
                modifier = Modifier
                    .fillMaxSize(),
                onVideoPrepared = {
                    videoPrepared.value = true
                }
            )
        }
        Box(
            Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = videoAlpha))
        )

        Box(
            Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        val linearGradient = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0xFF000000))
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(brush = linearGradient)
        )
    }
}

@Composable
private fun CardDate(modifier: Modifier = Modifier, date: Long) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFF8F8F8))
            .then(modifier)
            .height(28.dp)
            .wrapContentWidth()
    ) {
        Box(
            Modifier
                .size(28.dp)
                .background(Color(0xFFFF1A72)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = com.purpletear.news.presentation.R.drawable.news_calendar),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )

        }

        Box(
            Modifier
                .fillMaxHeight()
                .widthIn(min = 80.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            val context = LocalContext.current
            val isAvailable = RelativeDateFormatter.isTodayOrEarlier(date)
            val textToShow = if (isAvailable) {
                stringResource(id = R.string.game_announce_available)
            } else {
                RelativeDateFormatter.formatNewsDate(context, date)
            }
            Text(
                text = textToShow,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                fontSize = 10.sp
            )
        }
    }
}


@Composable
private fun ButtonText(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color = Color(0xFFF8F8F8),
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_press_scale"
    )

    Box(
        modifier
            .padding(vertical = 6.dp)
            .scale(scale)
            .clip(RoundedCornerShape(4.dp))
            .height(42.dp)
            .background(backgroundColor)
            .padding(horizontal = 28.dp, vertical = 6.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            fontSize = 12.sp
        )
    }
}