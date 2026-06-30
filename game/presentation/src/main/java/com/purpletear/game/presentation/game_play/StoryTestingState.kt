package com.purpletear.game.presentation.game_play

import androidx.annotation.Keep
import com.purpletear.sutoko.game.model.chapter.ChapterGraph

/**
 * Transport-level state of the SSE channel managed by [StoryTestingCoordinator].
 */
@Keep
enum class StoryTestingConnectionState {
    IDLE,
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
}

/**
 * State exposed by [StoryTestingCoordinator] to the UI and [GameEngineViewModel].
 *
 * @property isActive True while a test session is running.
 * @property isLoading True while connecting, downloading, or applying a package.
 * @property connectionState Transport state of the SSE channel.
 * @property error Fatal error message; the UI should stop testing when set.
 * @property currentGraph Latest loaded chapter graph; null until a package is applied.
 * @property targetNodeId Node the author asked to play from; null until PLAY_FROM_NODE.
 * @property playRequestCount Incremented each time a new play request is issued, so the UI can
 *           react to repeated requests for the same node.
 * @property pendingNodeId Most recent PLAY_FROM_NODE node not yet loaded.
 * @property currentChapterId Backend chapter UUID currently being tested.
 * @property graphVersion Incremented each time a new [currentGraph] is loaded. The UI can compare
 *           this value to detect a fresh graph without deep-equality checks.
 * @property lastWorkedOnChapterId Chapter UUID persisted from the author's previous test session.
 * @property initialChapterId Chapter UUID the coordinator decided to start this session from.
 */
@Keep
data class StoryTestingState(
    val isActive: Boolean = false,
    val isLoading: Boolean = false,
    val connectionState: StoryTestingConnectionState = StoryTestingConnectionState.IDLE,
    val error: String? = null,
    val currentGraph: ChapterGraph? = null,
    val targetNodeId: String? = null,
    val playRequestCount: Int = 0,
    val pendingNodeId: String? = null,
    val currentChapterId: String? = null,
    val graphVersion: Int = 0,
    val lastWorkedOnChapterId: String? = null,
    val initialChapterId: String? = null,
)
