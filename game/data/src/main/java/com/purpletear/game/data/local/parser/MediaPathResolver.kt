package com.purpletear.game.data.local.parser

import com.purpletear.sutoko.game.model.SUTOKO_MEDIA_BASE_URL
import com.purpletear.sutoko.game.provider.GamePathProvider
import java.io.File

private val AUDIO_EXTENSIONS = listOf("mp3", "ogg", "wav")

internal fun resolveImagePath(
    storagePath: String,
    gameId: String,
    legacyId: Int?,
    pathProvider: GamePathProvider
): String {
    if (storagePath.isBlank()) return ""
    val fileName = storagePath.substringAfterLast("/")
    val basePath = pathProvider.getStoryDirectoryPath(gameId, legacyId)
    return "$basePath${File.separator}assets${File.separator}$fileName"
}

internal fun resolveSoundPath(
    storagePath: String,
    gameId: String,
    legacyId: Int?,
    pathProvider: GamePathProvider
): String {
    val assetName = storagePath.substringAfterLast("/")
    if (assetName.isBlank()) return ""

    val storyPath = pathProvider.getStoryDirectoryPath(gameId, legacyId)
    val primary = "$storyPath${File.separator}assets${File.separator}$assetName"

    // Legacy archives placed sound files under medias/sounds/.
    // Try the new location first, then the legacy one.
    val candidates = listOf(
        primary,
        "$storyPath${File.separator}medias${File.separator}sounds${File.separator}$assetName"
    )

    val extensionCandidates = if ("." !in assetName) {
        candidates.flatMap { base ->
            AUDIO_EXTENSIONS.map { "$base.$it" }
        }
    } else {
        emptyList()
    }

    return (candidates + extensionCandidates).firstOrNull { File(it).exists() }
        ?: primary
}

/**
 * Visual-novel image/video layers may be missing from the downloaded archive. Fall back
 * to the remote media URL so the overlay can stream them; Coil and ExoPlayer both handle
 * http(s) URIs. Sounds don't use this: like every other sound node, they resolve through
 * [resolveSoundPath] to the bundled `assets/` directory.
 */
internal fun resolveVisualNovelMediaPath(
    storagePath: String,
    gameId: String,
    legacyId: Int?,
    pathProvider: GamePathProvider
): String {
    if (storagePath.contains("://")) return storagePath
    val local = resolveImagePath(storagePath, gameId, legacyId, pathProvider)
    return if (File(local).exists()) local else "$SUTOKO_MEDIA_BASE_URL$storagePath"
}
