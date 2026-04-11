package com.purpletear.sutoko.game.model

import com.purpletear.sutoko.game.provider.GamePathProvider
import java.io.File

/**
 * Base URL for Sutoko media files.
 * This is used to construct full URLs for game assets.
 */
const val SUTOKO_MEDIA_BASE_URL = "https://sutoko.com/media/"

/**
 * Returns the full URL for this asset's main image.
 * @return The complete URL string, or null if storagePath is blank
 */
fun Asset?.getFullUrl(): String? {
    if (this == null || storagePath.isBlank()) return null
    return "${SUTOKO_MEDIA_BASE_URL}$storagePath"
}

/**
 * Returns the full URL for this asset's thumbnail.
 * @return The complete URL string, or null if thumbnailStoragePath is blank
 */
fun Asset?.getThumbnailUrl(): String? {
    if (this == null || thumbnailStoragePath.isBlank()) return null
    return "${SUTOKO_MEDIA_BASE_URL}$thumbnailStoragePath"
}

/**
 * Resolves the local file system path for this asset.
 *
 * @param gameId The game/story ID for directory lookup
 * @param pathProvider Provider for story directory paths
 * @return The absolute local file path, or null if asset or storagePath is invalid
 */
fun Asset?.resolveLocalPath(gameId: String, pathProvider: GamePathProvider): String? {
    if (this == null || storagePath.isBlank()) return null
    val fileName = storagePath.substringAfterLast("/")
    val basePath = pathProvider.getStoryDirectoryPath(gameId)
    return "$basePath${File.separator}assets${File.separator}$fileName"
}
