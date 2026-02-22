package com.purpletear.version.presentation.components.announcement

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.Poppins
import com.purpletear.core.presentation.components.video.VideoComponent
import com.purpletear.news.presentation.R as NewsR

/**
 * Data class representing the state of a version announcement card.
 *
 * @property title The main title displayed on the card
 * @property subtitle The subtitle/description text
 * @property releaseDate The release date in epoch millis
 * @property videoUrl URL for the background video
 * @property isAvailable Whether the version is available today or earlier
 * @property isLoading Whether data is being loaded
 * @property isReminderEnabled Whether the reminder notification is enabled
 * @property buttonText The text to display on the action button
 * @property buttonLeadingIcon Optional icon resource for the button
 */
data class VersionAnnouncementState(
    val title: String,
    val subtitle: String,
    val releaseDate: Long,
    val videoUrl: String,
    val isAvailable: Boolean,
    val isLoading: Boolean = false,
    val isReminderEnabled: Boolean = false,
    val buttonText: String = "",
    val buttonLeadingIcon: Int? = null,
)

/**
 * Callbacks for user interactions with the announcement card.
 */
interface VersionAnnouncementCallbacks {
    fun onDismiss()
    fun onRemindMeClick()
    fun onUpdateClick()
    
    companion object {
        val EMPTY = object : VersionAnnouncementCallbacks {
            override fun onDismiss() {}
            override fun onRemindMeClick() {}
            override fun onUpdateClick() {}
        }
    }
}

/**
 * A generic, stateless announcement card for displaying future version information.
 *
 * @param state The current state of the card
 * @param callbacks Callbacks for user interactions
 * @param modifier Modifier for customizing the layout
 */
@Composable
fun VersionAnnouncementCard(
    state: VersionAnnouncementState,
    callbacks: VersionAnnouncementCallbacks,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    var showVideo by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose { showVideo = false }
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
            modifier = Modifier.matchParentSize(),
            videoUrl = state.videoUrl,
            showVideo = showVideo
        )

        // Close button
        CloseButton(
            onClick = {
                showVideo = false
                callbacks.onDismiss()
            }
        )

        // Content
        CardContent(
            state = state,
            callbacks = callbacks
        )

        // Loading overlay
        if (state.isLoading) {
            LoadingOverlay()
        }
    }
}

@Composable
private fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .size(28.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.25f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
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
}

@Composable
private fun CardContent(
    state: VersionAnnouncementState,
    callbacks: VersionAnnouncementCallbacks,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CardDate(releaseDate = state.releaseDate, isAvailable = state.isAvailable)

        Text(
            text = if (state.isAvailable) "Update now!" else "Soon available",
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            color = Color.White,
            fontSize = 11.5.sp
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = state.title,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFC2A69),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
        )

        Text(
            text = state.subtitle,
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFEAEAEA),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
        )

        ActionButton(
            state = state,
            onClick = {
                if (state.isAvailable) {
                    callbacks.onUpdateClick()
                } else {
                    callbacks.onRemindMeClick()
                }
            }
        )
    }
}

@Composable
private fun ActionButton(
    state: VersionAnnouncementState,
    onClick: () -> Unit,
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

    val buttonAlphaTarget = if (!state.isAvailable && state.isReminderEnabled) 0.5f else 1f
    val buttonAlpha by animateFloatAsState(
        targetValue = buttonAlphaTarget,
        animationSpec = tween(durationMillis = 200),
        label = "reminder_button_alpha"
    )

    val buttonBgTarget = if (!state.isAvailable && state.isReminderEnabled) {
        Color(0xFFE3FDE9)
    } else {
        Color(0xFFE2E3E2)
    }

    AnimatedContent(
        targetState = state.buttonText,
        transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
        label = "reminder_button_text_switch"
    ) { text ->
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .alpha(buttonAlpha)
                .scale(scale)
                .clip(RoundedCornerShape(4.dp))
                .height(42.dp)
                .background(buttonBgTarget)
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                state.buttonLeadingIcon?.let { iconRes ->
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(26.dp)
                            .padding(end = 8.dp)
                    )
                }
                Text(
                    text = text,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CardDate(
    releaseDate: Long,
    isAvailable: Boolean,
    modifier: Modifier = Modifier,
) {
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
                painter = painterResource(id = NewsR.drawable.news_calendar),
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
            Text(
                text = if (isAvailable) "Available" else formatDate(releaseDate),
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun BackgroundMedia(
    videoUrl: String,
    showVideo: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        val canStartVideo = remember { mutableStateOf(false) }
        val videoPrepared = remember { mutableStateOf(false) }
        val videoAlpha by animateFloatAsState(
            targetValue = if (videoPrepared.value) 0f else 1f,
            animationSpec = tween(durationMillis = 450),
            label = "video_fade_in"
        )

        LaunchedEffect(Unit) {
            withFrameNanos { }
            canStartVideo.value = true
        }

        if (showVideo && canStartVideo.value) {
            VideoComponent(
                url = videoUrl,
                modifier = Modifier.fillMaxSize(),
                onVideoPrepared = { videoPrepared.value = true }
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
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 1f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = Color(0xFFFC2A69),
            strokeWidth = 2.5.dp
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(timestamp))
}
