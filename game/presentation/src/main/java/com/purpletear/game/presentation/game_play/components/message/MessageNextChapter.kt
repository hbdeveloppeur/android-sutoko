package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sharedelements.theme.CrimsonTextFontFamily
import com.purpletear.game.debug.PreviewOverlayWrapper
import com.purpletear.game.presentation.R

@Preview
@Composable
private fun Preview() {
    PreviewOverlayWrapper(
        imageModifier = Modifier
            .padding(2.dp)
            .height(200.dp)
            .aspectRatio(589f / 241f),
        drawable = R.drawable.game_presentation_preview_manga_page,
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            MessageNextChapter()
        }
    }
}

@Composable
internal fun MessageNextChapter(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.game_presentation_message_next_chapter_title),
    showButton: Boolean = true,
    requiresAd: Boolean = false,
    isBusy: Boolean = false,
    onClick: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val hapticClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (!isBusy) onClick()
    }

    Column(
        modifier = modifier
            .padding(vertical = 24.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            color = Color.White,
            fontFamily = CrimsonTextFontFamily
        )
        if (showButton && isBusy) {
            androidx.compose.material3.CircularProgressIndicator(color = Color.White)
        } else if (showButton && requiresAd) {
            UnlockNextChapterButton(
                modifier = Modifier.testTag("game_next_chapter_ads_button"),
                onClick = hapticClick,
            )
        } else if (showButton) {
            PlayNextChapterButton(
                modifier = Modifier.testTag("game_next_chapter_button"),
                onClick = hapticClick,
            )
        }
    }
}
