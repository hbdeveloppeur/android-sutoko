package com.purpletear.game.presentation.game_catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.R

private val PlaceholderColor = Color.White.copy(alpha = 0.06f)

@Composable
private fun Modifier.loadingStories(): Modifier {
    val description = stringResource(R.string.game_presentation_catalog_loading)
    return clearAndSetSemantics {
        contentDescription = description
        progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
    }
}

/** Static placeholders: no endless shimmer competing with the initial image decode. */
@Composable
fun GameSquaresPlaceholder(modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(16.dp).padding(top = 8.dp).loadingStories(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        repeat(4) {
            Column(Modifier.weight(1f).padding(1.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(PlaceholderColor))
                Box(contentAlignment = Alignment.BottomCenter) {
                    GameSquareTitle(title = " ", modifier = Modifier.alpha(0f))
                    Box(Modifier.size(48.dp, 10.dp).clip(RoundedCornerShape(4.dp)).background(PlaceholderColor))
                }
            }
        }
    }
}

@Composable
fun GamePosterRowPlaceholder(modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().clipToBounds().loadingStories().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(3) {
            Box(Modifier.width(120.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(8.dp)).background(PlaceholderColor))
        }
    }
}

@Composable
fun GameCardPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().aspectRatio(GAME_CARD_ASPECT).background(PlaceholderColor).loadingStories())
}
