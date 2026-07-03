package com.purpletear.game.presentation.game_play.liveupdate

import com.purpletear.sutoko.game.model.chapter.ChapterGraph

/**
 * Determines whether a [PLAY_FROM_NODE](TestEvent.PlayFromNode) request for [chapterId] can be
 * satisfied immediately, needs a cached graph loaded, or must wait.
 */
internal sealed class PlayFromNodeGraphState {
    data object Ready : PlayFromNodeGraphState()
    data class Cached(val extractedDir: String) : PlayFromNodeGraphState()
    data object Missing : PlayFromNodeGraphState()
}

/**
 * Resolves the graph state for an explicit play request.
 *
 * The result is deterministic and side-effect free so it can be unit tested without mocking the
 * coordinator's dependencies.
 */
internal object StoryLiveUpdatePlayFromNodeResolver {

    fun resolve(
        chapterId: String,
        currentGraph: ChapterGraph?,
        extractedDirectories: Map<String, String>,
    ): PlayFromNodeGraphState {
        val graph = currentGraph
        if (graph != null && graph.chapterCode == chapterId) {
            return PlayFromNodeGraphState.Ready
        }

        val extractedDir = extractedDirectories[chapterId]
        return if (extractedDir != null) {
            PlayFromNodeGraphState.Cached(extractedDir)
        } else {
            PlayFromNodeGraphState.Missing
        }
    }
}
