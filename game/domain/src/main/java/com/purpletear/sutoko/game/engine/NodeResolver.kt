package com.purpletear.sutoko.game.engine

import androidx.annotation.Keep
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handles all node navigation logic.
 * Separated from GameEngine for testability.
 */
class NodeResolver @Inject constructor() {

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
            GameEngineLogger.d("NAV") {
                "Node ${currentNode.id} has ${messageChoices.size} message choices → await input"
            }
            return ResolutionResult.AwaitChoice(messageChoices)
        }

        val target = edges.first().target
        assert(target.isNotBlank())
        GameEngineLogger.d("NAV") { "Node ${currentNode.id} follows edge → $target" }
        return ResolutionResult.NextNode(target)
    }

    private companion object {
        const val MIN_CHOICE_COUNT = 2
    }
}
