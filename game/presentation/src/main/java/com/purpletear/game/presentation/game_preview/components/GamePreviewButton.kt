package com.purpletear.game.presentation.game_preview.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.R
import com.purpletear.core.presentation.components.icon.Icon
import com.purpletear.core.presentation.components.icon.IconComposable
import com.purpletear.game.presentation.game_preview.Background
import com.purpletear.game.presentation.game_preview.toBrush

private val WorkSansSemiBold = FontFamily(
    Font(R.font.shared_elements_font_worksans_semibold, FontWeight.SemiBold)
)
private val WorkSansRegular = FontFamily(
    Font(R.font.shared_elements_font_worksans_regular, FontWeight.Normal)
)


@Composable
internal fun GamePreviewButton(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    title: String? = null,
    subtitle: String? = null,
    icon: Icon? = null,
    background: Background = Background.Solid(Color(0xFF333333)),
    isLoading: Boolean = false,
    progress: Float? = null,
    isEnabled: Boolean = true,
    iconAlignment: Alignment = Alignment.CenterEnd,
    accessibilityLabel: String? = null,
) {

    val haptic = LocalHapticFeedback.current
    val displayedProgress by animateFloatAsState(
        targetValue = progress?.coerceIn(0f, 1f) ?: 0f,
        animationSpec = tween(180),
        label = "downloadProgress",
    )

    Box(
        modifier = modifier
            .semantics { accessibilityLabel?.let { contentDescription = it } }
            .clickable(
                enabled = isEnabled && !isLoading && progress == null && onClick != null,
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick?.invoke()
                }
            )
            .clip(RoundedCornerShape(5.dp))
            .heightIn(min = 50.dp)
            .background(brush = background.toBrush()),
        contentAlignment = Alignment.Center
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isLoading || (icon != null && iconAlignment == Alignment.CenterEnd)) 38.dp else 12.dp,
                    vertical = 10.dp,
                )
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (!isEnabled) 0.45f else 1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(2.dp)
            ) {
                Text(
                    text = title ?: "",
                    fontFamily = WorkSansSemiBold,
                    fontSize = 12.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                subtitle?.let {
                    Text(
                        text = it,
                        fontFamily = WorkSansRegular,
                        fontSize = 9.5.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        var iconModifier = when (icon) {
            is Icon.LottieAnimation -> {
                Modifier
                    .align(
                        Alignment.Center
                    )
                    .size(40.dp)
            }

            else -> {
                Modifier
                    .padding(end = if (iconAlignment == Alignment.CenterEnd) 14.dp else 0.dp)
                    .size(15.dp)
                    .align(iconAlignment)
                    .alpha(if (isLoading) 0.2f else 1f)
            }
        }

        iconModifier = if (!isEnabled) iconModifier.then(Modifier.alpha(0.2f)) else iconModifier

        // Icon on the right if provided
        icon?.takeUnless { isLoading }?.let {
            IconComposable(
                icon = it,
                modifier = iconModifier,
            )
        }


        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(12.dp)
                    .size(16.dp)
                    .align(Alignment.CenterEnd),
                color = Color.LightGray,
                strokeWidth = 2.dp
            )
        }

        if (progress != null) {
            LinearProgressIndicator(
                progress = { displayedProgress },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.15f),
            )
        }

    }
}
