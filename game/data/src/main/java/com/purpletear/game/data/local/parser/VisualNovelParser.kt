package com.purpletear.game.data.local.parser

import com.purpletear.game.data.local.dto.NodeDataDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.provider.GamePathProvider

// Visual novel: bounds and canvas-compatible defaults (see canvas visual-novel-node types).
private const val MAX_VISUAL_NOVEL_LAYERS = 8
private const val MAX_VISUAL_NOVEL_DIALOGS = 16
private const val MAX_VISUAL_NOVEL_SOUNDS = 4
private const val MAX_VISUAL_NOVEL_TEXT_LEN = 500
private const val DEFAULT_VISUAL_NOVEL_THEME_COLOR = "#332F63"
private const val DEFAULT_VISUAL_NOVEL_THEME_OPACITY = 0.7f
private val VISUAL_NOVEL_THEME_COLOR_REGEX = Regex("^#[0-9A-Fa-f]{6}$")

internal fun parseVisualNovel(
    dto: NodeDto,
    data: NodeDataDto?,
    gameId: String,
    legacyId: Int?,
    pathProvider: GamePathProvider
): Node? {
    val id = dto.id

    // Bound parsing work and drop malformed entries rather than crashing.
    val layers = data?.layers.orEmpty()
        .take(MAX_VISUAL_NOVEL_LAYERS)
        .mapNotNull { layer ->
            val path = layer.storagePath?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            Node.VisualNovel.Layer(
                path = resolveVisualNovelMediaPath(path, gameId, legacyId, pathProvider),
                assetId = layer.assetId,
                isVideo = layer.type?.trim()?.lowercase() == "video",
            )
        }
    // Degenerate node (nothing to show): drop it like a malformed manga page.
    if (layers.isEmpty()) return null

    val dialogs = data?.dialogs.orEmpty()
        .take(MAX_VISUAL_NOVEL_DIALOGS)
        .mapNotNull { dialog ->
            val text = dialog.text?.trim()
                ?.take(MAX_VISUAL_NOVEL_TEXT_LEN)
                ?.takeIf { it.isNotEmpty() }
                ?: return@mapNotNull null
            val soundPath = dialog.soundStoragePath?.trim()?.takeIf { it.isNotEmpty() }
                ?.let { resolveSoundPath(it, gameId, legacyId, pathProvider) }
            Node.VisualNovel.Dialog(
                text = text,
                // Durations are authored in milliseconds. When a sounded dialog has no
                // explicit duration, the sound duration paces the sequence instead.
                durationMs = dialog.duration?.takeIf { it >= 0 }?.toLong()
                    ?: dialog.soundDurationMs?.takeIf { it >= 0 && soundPath != null },
                delayMs = dialog.delay?.takeIf { it >= 0 }?.toLong() ?: 0,
                soundPath = soundPath,
            )
        }

    val sounds = data?.sounds.orEmpty()
        .take(MAX_VISUAL_NOVEL_SOUNDS)
        .mapNotNull { sound ->
            val path = sound.storagePath?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            Node.VisualNovel.Sound(
                path = resolveSoundPath(path, gameId, legacyId, pathProvider),
                volume = sound.volume?.coerceIn(0f, 1f) ?: 1f,
                loop = sound.loop ?: false,
            )
        }

    val theme = Node.VisualNovel.Theme(
        colorHex = data?.theme?.color?.trim()
            ?.takeIf { VISUAL_NOVEL_THEME_COLOR_REGEX.matches(it) }
            ?: DEFAULT_VISUAL_NOVEL_THEME_COLOR,
        opacity = data?.theme?.opacity?.coerceIn(0f, 1f) ?: DEFAULT_VISUAL_NOVEL_THEME_OPACITY,
    )

    return Node.VisualNovel(
        id = id,
        title = data?.title?.trim()?.take(MAX_VISUAL_NOVEL_TEXT_LEN)?.takeIf { it.isNotEmpty() },
        layers = layers,
        dialogs = dialogs,
        sounds = sounds,
        theme = theme,
        delayMs = data?.delay ?: 0,
    )
}
