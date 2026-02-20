package fr.purpletear.sutoko.screens.create.components.story_cover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.purpletear.sutoko.screens.create.components.story_card.StoryCard

private const val GRADIENT_START_ALPHA = 0.9f
private const val GRADIENT_END_ALPHA = 0.00001f
private val CARD_CORNER_RADIUS = 16.dp
private val COVER_HEIGHT = 180.dp
private val GRADIENT_HEIGHT = 100.dp
private val CARD_BOTTOM_PADDING = 16.dp
private val BORDER_WIDTH = 1.dp


@Composable
internal fun StoryCover(
    modifier: Modifier = Modifier,
    coverUrl: String,
    title: String,
    author: String,
    thumbnailUrl: String
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(COVER_HEIGHT)
            .border(
                width = BORDER_WIDTH,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(CARD_CORNER_RADIUS)
            )
            .clip(RoundedCornerShape(CARD_CORNER_RADIUS))
    ) {
        CoverImage(coverUrl = coverUrl)
        Filter()
        BottomGradient(modifier = Modifier.align(Alignment.BottomStart))
        StoryCardInfo(
            title = title,
            author = author,
            thumbnailUrl = thumbnailUrl
        )
    }
}

@Composable
private fun CoverImage(coverUrl: String) {
    AsyncImage(
        model = coverUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun Filter(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(.1f))
    ) {}
}

@Composable
private fun BottomGradient(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(GRADIENT_HEIGHT)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = GRADIENT_START_ALPHA),
                        Color.Black.copy(alpha = GRADIENT_END_ALPHA)
                    ),
                    startY = Float.POSITIVE_INFINITY,
                    endY = 0f
                )
            )
    )
}

@Composable
private fun StoryCardInfo(
    title: String,
    author: String,
    thumbnailUrl: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = CARD_BOTTOM_PADDING),
        contentAlignment = Alignment.BottomStart
    ) {
        StoryCard(
            title = title,
            author = author,
            imageUrl = thumbnailUrl,
            showGetButton = false
        )
    }
}
