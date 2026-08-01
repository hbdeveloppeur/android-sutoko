package com.purpletear.game.presentation.game_catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.game.model.game.GameCatalog

/** Portrait poster aspect ratio (width / height), Netflix-style 2:3. */
private const val POSTER_ASPECT = 2f / 3f

/**
 * Horizontally scrollable row of portrait story posters (Netflix-style).
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
            .width(172.dp)
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
        if (isFavorite) {
            Icon(
                painter = painterResource(R.drawable.game_star_selected),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(14.dp),
            )
        }
    }
}
