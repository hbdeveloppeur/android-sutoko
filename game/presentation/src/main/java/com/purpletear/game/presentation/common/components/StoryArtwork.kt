package com.purpletear.game.presentation.common.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.decode.DataSource
import coil.request.ImageRequest
import com.purpletear.game.presentation.common.storyImageCrossfadeMillis

/** A single visual reveal for a story's background, title, and badges. */
@Composable
internal fun StoryArtwork(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    titleUrl: String? = null,
    title: String? = null,
    titleModifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    key(imageUrl, titleUrl) {
        // Null means still loading; true means immediately reusable from memory (or absent).
        var backgroundCached by remember { mutableStateOf<Boolean?>(if (imageUrl.isNullOrBlank()) true else null) }
        var titleCached by remember { mutableStateOf<Boolean?>(if (titleUrl.isNullOrBlank()) true else null) }
        val context = LocalContext.current
        val request = remember(context, imageUrl) {
            ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(context.storyImageCrossfadeMillis())
                .build()
        }
        ArtworkReveal(
            isReady = backgroundCached != null && titleCached != null,
            isCached = backgroundCached == true && titleCached == true,
            modifier = modifier,
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = request,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    contentDescription = null,
                    onSuccess = { backgroundCached = it.result.dataSource == DataSource.MEMORY_CACHE },
                    onError = { backgroundCached = false },
                )
            }
            if (title != null || !titleUrl.isNullOrBlank()) {
                GameLogo(
                    titleUrl = titleUrl,
                    title = title,
                    modifier = titleModifier,
                    onImageSettled = { titleCached = it },
                )
            }
            content()
        }
    }
}
