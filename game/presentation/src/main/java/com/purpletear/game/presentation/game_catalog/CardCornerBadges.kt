package com.purpletear.game.presentation.game_catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.R

/**
 * Top-right corner badges of a story card: a small coin when the story is
 * paying, stacked above the favorite star. Both are decorative — the card
 * semantics already describe the story.
 */
@Composable
fun BoxScope.CardCornerBadges(isPremium: Boolean, isFavorite: Boolean) {
    if (!isPremium && !isFavorite) return
    Column(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (isPremium) {
            Image(
                painter = painterResource(R.drawable.game_presentation_ic_dollar_coin),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
            )
        }
        if (isFavorite) {
            Icon(
                painter = painterResource(R.drawable.game_star_selected),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
