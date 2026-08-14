package com.purpletear.sutoko.game.engine

import androidx.annotation.Keep
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handles all node navigation logic.
 * Separated from GameEngine for testability.
 */
class NodeResolver @Inject constructor(
    private val memory: GameMemory,
) {

    sealed class ResolutionResult {
        @Keep
        data class NextNode(val nodeId: String) : ResolutionResult()
        data object NodeNextChapter : ResolutionResult()
        @Keep
        data class AwaitChoice(
            val choices: List<HandlerEffect.ShowChoices.Choice>
        ) : ResolutionResult()
        @Keep
        data class Error(val message: String) : ResolutionResult()
    }

    fun resolveNextNode(
        graph: ChapterGraph,
        currentNode: Node,
        forceNodId: String?
    ): ResolutionResult {
        if (forceNodId != null) {
            GameEngineLogger.d("NAV") { "Explicit nextNodeId overrides edges → $forceNodId" }
            return ResolutionResult.NextNode(forceNodId)
        }

        // Chapter change node = end of current chapter
        if (currentNode is Node.ChapterChange) {
            GameEngineLogger.d("NAV") { "Chapter change node ${currentNode.id} → chapter finished" }
            return ResolutionResult.NodeNextChapter
        }

        // Resolve via edges
        val nextEdges = graph.getNextEdges(currentNode.id)

        return when {
            nextEdges.isEmpty() -> {
                GameEngineLogger.d("NAV") { "Node ${currentNode.id} has no outgoing edges → chapter finished" }
                ResolutionResult.NodeNextChapter
            }

            else -> resolveFromEdges(graph, currentNode, nextEdges)
        }
    }

    private fun resolveFromEdges(
        graph: ChapterGraph,
        currentNode: Node,
        edges: List<com.purpletear.sutoko.game.model.chapter.Edge>
    ): ResolutionResult {
        val messageChoices = edges.mapNotNull { edge ->
            val target = graph.getNode(edge.target) ?: return@mapNotNull null
            if (target is Node.Message) {
                HandlerEffect.ShowChoices.Choice(
                    id = target.id,
                    text = target.text,
                    nextNodeId = target.id
                )
            } else {
                null
            }
        }

        if (messageChoices.size >= MIN_CHOICE_COUNT) {
            val visibleChoices = filterVisibleChoices(graph, messageChoices)
            GameEngineLogger.d("NAV") {
                "Node ${currentNode.id} has ${messageChoices.size} message choices " +
                    "(${visibleChoices.size} visible) → await input"
            }
            return ResolutionResult.AwaitChoice(visibleChoices)
        }

        val target = edges.first().target
        assert(target.isNotBlank())
        GameEngineLogger.d("NAV") { "Node ${currentNode.id} follows edge → $target" }
        return ResolutionResult.NextNode(target)
    }

    /**
     * A choice backed by a [Node.Message] with a condition is shown only when the
     * condition holds (memory[key] == expectedValue; a missing key never matches).
     * If every choice is gated out, fall back to the unfiltered list so the player
     * is never soft-locked by a story authoring mistake.
     */
    private fun filterVisibleChoices(
        graph: ChapterGraph,
        choices: List<HandlerEffect.ShowChoices.Choice>
    ): List<HandlerEffect.ShowChoices.Choice> {
        val visible = choices.filter { choice ->
            val condition = (graph.getNode(choice.id) as? Node.Message)?.condition
                ?: return@filter true
            memory.get(condition.key) == condition.expectedValue
        }
        if (visible.isEmpty()) {
            GameEngineLogger.d("NAV") {
                "All ${choices.size} choices filtered out by conditions → keeping unfiltered list"
            }
            return choices
        }
        return visible
    }

    private companion object {
        const val MIN_CHOICE_COUNT = 2
    }
}
