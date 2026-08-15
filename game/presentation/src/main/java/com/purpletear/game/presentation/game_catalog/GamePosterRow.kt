package com.purpletear.game.presentation.game_catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.common.components.GameLogo
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.isPremium
import kotlin.math.roundToInt

/** Portrait poster aspect ratio (width / height), 2:3. */
private const val POSTER_ASPECT = 2f / 3f

/** Vertical center of the title image, as a fraction of the card height. */
private const val POSTER_TITLE_CENTER_Y_FRACTION = 0.75f

/** Max width of the title image, as a fraction of the card width. */
private const val POSTER_TITLE_WIDTH_FRACTION = 0.8f

/**
 * Horizontally scrollable row of portrait story posters.
 * Renders stories whose [GameCatalog.cardLayout] is VERTICAL.
 */
@Composable
fun GamePosterRow(
    stories: List<GameCatalog>,
    favoriteIds: Set<String>,
    onTap: (GameCatalog) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = stories, key = { it.id }) { story ->
            GamePosterCard(
                gameCatalog = story,
                isFavorite = story.id in favoriteIds,
                onTap = onTap,
            )
        }
    }
}

/**
 * Sizes the title image to at most [POSTER_TITLE_WIDTH_FRACTION] of the card
 * width (aspect ratio preserved by the image's Fit scaling) and places it
 * horizontally centered, vertically centered at [POSTER_TITLE_CENTER_Y_FRACTION]
 * of the card height.
 */
private fun Modifier.posterTitleRect(): Modifier = layout { measurable, constraints ->
    val maxWidth = (constraints.maxWidth * POSTER_TITLE_WIDTH_FRACTION).roundToInt()
    val placeable = measurable.measure(
        constraints.copy(minWidth = 0, minHeight = 0, maxWidth = maxWidth)
    )
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.place(
            x = (constraints.maxWidth - placeable.width) / 2,
            y = (constraints.maxHeight * POSTER_TITLE_CENTER_Y_FRACTION - placeable.height / 2f)
                .roundToInt(),
        )
    }
}

/**
 * Portrait story card: the [GameCatalog.verticalBanner] image cropped to a
 * 2:3 poster with rounded corners. Tap behaviour matches the banner GameCard.
 */
@Composable
fun GamePosterCard(
    gameCatalog: GameCatalog,
    isFavorite: Boolean,
    onTap: (GameCatalog) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(120.dp)
            .aspectRatio(POSTER_ASPECT)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .semantics { contentDescription = gameCatalog.metadata.title }
            .clickable { onTap(gameCatalog) }
    ) {
        val context = LocalContext.current
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = gameCatalog.verticalBannerImageRequest(context)
                ?: ImageRequest.Builder(context).build(),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )
        GameLogo(
            titleUrl = remember(gameCatalog.title) { gameCatalog.titleUrl() },
            modifier = Modifier.posterTitleRect(),
        )
        CardCornerBadges(
            isPremium = gameCatalog.isPremium(),
            isFavorite = isFavorite,
        )
    }
}
