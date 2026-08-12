package com.purpletear.game.data.local.parser

import com.purpletear.game.data.local.dto.MangaMessageDto
import com.purpletear.game.data.local.dto.NodeDataDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.provider.GamePathProvider

// Manga page: bounds and legacy-compatible defaults (see legacy MangaHelper.parseMessage).
private const val MAX_MANGA_MESSAGES = 32
private const val MAX_MANGA_SENTENCE_LEN = 500
private const val DEFAULT_MANGA_SIZE = 30f
private const val DEFAULT_MANGA_X = 1f
private const val DEFAULT_MANGA_Y = 1f
private const val DEFAULT_MANGA_W = 10f

internal fun parseMangaPage(
    dto: NodeDto,
    data: NodeDataDto?,
    gameId: String,
    legacyId: Int?,
    pathProvider: GamePathProvider
): Node? {
    val id = dto.id
    val fileName =
        data?.storagePath?.trim()?.takeIf { it.isNotEmpty() }?.substringAfterLast("/")
    require(fileName != null) { "manga-page node $id missing storagePath" }

    // Bound parsing work and drop malformed entries rather than crashing.
    val messages = data.messages.orEmpty()
        .take(MAX_MANGA_MESSAGES)
        .mapNotNull { it.toMangaMessage() }
    if (messages.isEmpty()) return null

    return Node.MangaPage(
        id = id,
        imageUrl = resolveImagePath(fileName, gameId, legacyId, pathProvider),
        assetId = data.assetId,
        messages = messages,
        waitMs = data.duration ?: 0,
        seenMs = data.delay ?: 0,
    )
}

private fun MangaMessageDto.toMangaMessage(): Node.MangaPage.MangaMessage? {
    val text = sentence?.trim()
        ?.take(MAX_MANGA_SENTENCE_LEN)
        ?.takeIf { it.isNotEmpty() }
        ?: return null
    return Node.MangaPage.MangaMessage(
        text = text,
        size = size ?: DEFAULT_MANGA_SIZE,
        x = x ?: DEFAULT_MANGA_X,
        y = y ?: DEFAULT_MANGA_Y,
        w = w ?: DEFAULT_MANGA_W,
    )
}
