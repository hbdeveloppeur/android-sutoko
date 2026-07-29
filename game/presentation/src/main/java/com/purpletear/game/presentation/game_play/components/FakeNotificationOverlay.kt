package com.purpletear.game.presentation.game_play.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_play.state.FakeNotificationUi
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val EXIT_ANIMATION_MS = 240

private val ScrimColor = Color(0x4F000000)
private val CardColor = Color.White
private val TitleColor = Color(0xFF010101)
private val SubtitleColor = Color(0xFF313131)
private val ActionTextColor = Color(0xFF2C6F92)
private val AvatarOverlayColor = Color(0x44FFFFFF)

/**
 * Decorative, non-clickable fake system notification overlay (port of the legacy
 * `SmsNotification`). Drops in from above the top edge over a dimmed scrim (like a real
 * system banner), holds for [FakeNotificationUi.durationMs], lifts back out the same way,
 * then calls [onDismissed]. The scrim swallows all touches; the card itself has no click action.
 */
@Composable
internal fun FakeNotificationOverlay(
    notification: FakeNotificationUi,
    onDismissed: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val topPaddingPx = with(density) { (maxHeight * 0.066f).toPx() }
        val cardHeightPx = with(density) { 120.dp.toPx() }
        // Hidden position: fully above the visible area.
        val hiddenYPx = -(topPaddingPx + cardHeightPx)

        // 0f = hidden above the screen, 1f = in place.
        val progress = remember { Animatable(0f) }

        LaunchedEffect(notification) {
            // Drops in like a real system banner, with a soft settle.
            progress.animateTo(
                1f,
                spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
            )
            if (notification.durationMs > 0) {
                delay(notification.durationMs)
            }
            // Lifts back out the way it came, accelerating away.
            progress.animateTo(0f, tween(EXIT_ANIMATION_MS, easing = FastOutLinearInEasing))
            onDismissed()
        }

        val visible = progress.value.coerceIn(0f, 1f)
        Box(
            Modifier
                .fillMaxSize()
                .background(ScrimColor.copy(alpha = ScrimColor.alpha * visible))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        )

        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = maxHeight * 0.066f)
                .fillMaxWidth(0.914f)
                .offset { IntOffset(0, (hiddenYPx * (1f - progress.value)).roundToInt()) }
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardColor)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp, end = 110.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notification.title,
                    color = TitleColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = notification.subtitle,
                    color = SubtitleColor,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = notification.actionText,
                    color = ActionTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 30.dp)
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = notification.avatarPath ?: R.drawable.game_presentation_tmp_avatar,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(AvatarOverlayColor)
                )
            }
        }
    }
}
