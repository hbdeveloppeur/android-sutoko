package com.purpletear.game.presentation.game_play.layout

import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.core.domain.logger.exception
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Resolves which character ids render on the right side. The chapter archive's
 * `layout.json` wins when it declares sides; the Room/API value is the fallback.
 * An empty result means the screen keeps its legacy main-character rule.
 */
class RightSideLayoutResolver(
    private val chapterRepository: ChapterRepository,
    private val logger: Logger,
    private val logResolution: (ids: Set<Int>, source: String) -> Unit = { _, _ -> },
) {
    private var archiveRightSideIds: Set<Int>? = null
    private var roomRightSideIds: Set<Int> = emptySet()

    fun onGraphLoaded(graph: ChapterGraph): Set<Int> {
        archiveRightSideIds = graph.rightSideCharacterIds.toSet()
        return resolveAndLog()
    }

    fun observeRoomLayout(gameId: String, chapterCode: String): Flow<Set<Int>> =
        chapterRepository.observeChapters(gameId)
            .catch { error ->
                logger.exception(error) { "chapter layout observation failed" }
                emit(emptyList())
            }
            .map { chapters ->
                roomRightSideIds = chapters
                    .firstOrNull { it.normalizedCode == chapterCode.lowercase() }
                    ?.rightSideCharacterIds
                    .orEmpty()
                    .toSet()
                resolveAndLog()
            }

    private fun resolveAndLog(): Set<Int> {
        val archiveIds = archiveRightSideIds?.takeIf { it.isNotEmpty() }
        val effective = archiveIds ?: roomRightSideIds
        val source = when {
            archiveIds != null -> "archive layout.json"
            roomRightSideIds.isNotEmpty() -> "api/room"
            else -> "none (legacy main-character rule)"
        }
        logResolution(effective, source)
        return effective
    }
}
