package com.purpletear.game.presentation.game_preview.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.R

/**
 * Top-right sound toggle of the GamePreview screen.
 * Mutes or restores the story's menu ambience; the choice is persisted.
 */
@Composable
fun GamePreviewSoundButton(
    isMuted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val description = stringResource(
        if (isMuted) R.string.game_presentation_game_preview_sound_on
        else R.string.game_presentation_game_preview_sound_off
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .alpha(0.7f)
            .semantics { contentDescription = description }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggle()
            },
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(targetState = isMuted, label = "menuSound") { muted ->
            Icon(
                painter = painterResource(
                    if (muted) R.drawable.game_presentation_volume_off
                    else R.drawable.game_presentation_volume_on
                ),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
