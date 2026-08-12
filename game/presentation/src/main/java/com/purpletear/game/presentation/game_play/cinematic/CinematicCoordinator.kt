package com.purpletear.game.presentation.game_play.cinematic

import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.core.domain.logger.exception
import com.purpletear.sutoko.game.engine.GameEngine
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.model.chapter.extractCinematicBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Owns the cinematic slice of a chapter: extracting the linear body, parking the SMS
 * engine, requesting navigation, then resuming at the node after `[intro=end]`.
 */
class CinematicCoordinator(
    private val gameEngine: GameEngine,
    private val logger: Logger,
    private val scope: CoroutineScope,
    private val updateState: (transform: (GameUiState) -> GameUiState) -> Unit,
) {
    private val _navigateToCinematic = Channel<Unit>(Channel.BUFFERED)
    val navigateToCinematic: Flow<Unit> = _navigateToCinematic.receiveAsFlow()

    private var currentGraph: ChapterGraph? = null
    private var cinematicResumeNodeId: String? = null

    fun onGraphLoaded(graph: ChapterGraph) {
        currentGraph = graph
    }

    fun handle(effect: HandlerEffect.EnterCinematic) {
        val graph = currentGraph
        if (graph == null) {
            logger.exception(IllegalStateException("EnterCinematic with no currentGraph")) {
                "Cannot enter cinematic: no graph loaded"
            }
            return
        }

        extractCinematicBody(graph, effect.startNodeId, effect.endNodeId).fold(
            onSuccess = { body -> enterValidCinematic(graph, effect, body) },
            onFailure = { error -> skipInvalidCinematic(graph, effect, error) }
        )
    }

    /** Called when the cinematic body is exhausted (or cancelled). */
    fun onCinematicFinished() = resumeFromCinematic()

    private fun enterValidCinematic(
        graph: ChapterGraph,
        effect: HandlerEffect.EnterCinematic,
        body: List<Node>,
    ) {
        val resumeNodeId = graph.singleSuccessor(effect.endNodeId)
        assert(resumeNodeId == null || graph.getNode(resumeNodeId) != null) {
            "Cinematic resume node $resumeNodeId not found in ${graph.chapterCode}"
        }
        cinematicResumeNodeId = resumeNodeId
        if (body.isEmpty()) {
            resumeFromCinematic()
        } else {
            updateState { it.copy(cinematicBody = body, isCinematicActive = true) }
            _navigateToCinematic.trySend(Unit)
        }
    }

    private fun skipInvalidCinematic(
        graph: ChapterGraph,
        effect: HandlerEffect.EnterCinematic,
        error: Throwable,
    ) {
        logger.exception(error) {
            "Invalid cinematic from ${effect.startNodeId}; skipping"
        }
        val fallback = graph.singleSuccessor(effect.startNodeId)
        cinematicResumeNodeId = null
        updateState { it.copy(cinematicBody = emptyList(), isCinematicActive = false) }
        if (fallback != null) {
            scope.launch {
                gameEngine.resume()
                gameEngine.startFromNode(fallback)
            }
        } else {
            gameEngine.resume()
        }
    }

    private fun resumeFromCinematic() {
        val resumeNodeId = cinematicResumeNodeId
        cinematicResumeNodeId = null
        updateState { it.copy(cinematicBody = emptyList(), isCinematicActive = false) }

        gameEngine.resume()

        if (resumeNodeId != null) {
            scope.launch { gameEngine.startFromNode(resumeNodeId) }
        }
    }
}
