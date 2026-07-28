package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.common.components.SimpleButton
import com.purpletear.game.presentation.common.components.SimpleButtonIconSide
import com.purpletear.game.presentation.game_preview.components.formatReleaseDate

@Composable
internal fun MessageNextChapterUnavailable(
    modifier: Modifier = Modifier,
    gameLogoUrl: String,
    releaseDateSeconds: Long? = null,
    onClick: () -> Unit = {},
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(340.dp),
        contentAlignment = Alignment.Center
    ) {
        val date = releaseDateSeconds?.let { formatReleaseDate(it) }
        Background()
        Content(
            subtitle = stringResource(R.string.game_presentation_message_next_chapter_unavailable_subtitle),
            subtitleHighlight = stringResource(R.string.game_presentation_message_next_chapter_unavailable_subtitle_highlight),
            releaseDate = date?.let {
                stringResource(
                    R.string.game_presentation_message_next_chapter_unavailable_date,
                    it,
                )
            },
            releaseDateHighlight = date.orEmpty(),
            backText = stringResource(R.string.game_presentation_message_chapter_trial_finished_back_button),
            gameLogoUrl = gameLogoUrl,
            onClickBackButton = onClick,
        )
        Motifs()
    }
}


@Composable
private fun Background() {
    AsyncImage(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.2f),
        model = R.drawable.game_presentation_gradient_blue_circle_bottom,
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun Content(
    subtitle: String,
    subtitleHighlight: String,
    backText: String,
    gameLogoUrl: String?,
    releaseDate: String? = null,
    releaseDateHighlight: String = "",
    onClickBackButton: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (gameLogoUrl != null) {
            GameLogo(url = gameLogoUrl)
        }
        Subtitle(text = subtitle, highlight = subtitleHighlight)
        if (releaseDate != null) {
            ReleaseDate(text = releaseDate, highlight = releaseDateHighlight)
        }

        SimpleButton(
            text = backText,
            fontSize = 12.sp,
            onClick = onClickBackButton,
            horizontalPadding = 14.dp,
            verticalPadding = 6.dp,
            iconSide = SimpleButtonIconSide.Start,
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
        )
    }
}
