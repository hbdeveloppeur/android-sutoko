package com.purpletear.game.presentation.game_catalog

import android.content.Context
import coil.request.ImageRequest
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.getFullUrl

/**
 * Gets the banner URL for a game.
 * @return The URL string for the banner image, or null if not available.
 */
fun GameCatalog.bannerUrl(): String? = banner.getFullUrl()

/**
 * Creates an ImageRequest for the game's banner.
 * @param context The Android context for the request builder.
 * @return An ImageRequest configured for the banner image, or null if not available.
 */
fun GameCatalog.bannerImageRequest(context: Context): ImageRequest? {
    val url = bannerUrl() ?: return null
    return ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)
        .build()
}

/**
 * Gets the logo URL for a game.
 * @return The URL string for the logo image, or null if not available.
 */
fun GameCatalog.titleUrl(): String? = title.getFullUrl()

