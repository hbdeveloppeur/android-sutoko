package com.purpletear.game.presentation.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Displays the game's logo (title asset), scaled to fit without distortion.
 * Nothing is drawn when the game has no logo.
 */
@Composable
internal fun GameLogo(
    titleUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    if (titleUrl.isNullOrBlank()) return
    val context = LocalContext.current
    val request = remember(titleUrl) {
        ImageRequest.Builder(context)
            .data(titleUrl)
            .crossfade(true)
            .build()
    }
    AsyncImage(
        modifier = modifier,
        model = request,
        contentScale = ContentScale.Fit,
        contentDescription = contentDescription,
    )
}
