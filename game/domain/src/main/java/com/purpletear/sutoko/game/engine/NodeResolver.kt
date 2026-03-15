package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handles all node navigation logic.
 * Separated from GameEngine for testability.
 */
class NodeResolver @Inject constructor() {

    sealed class ResolutionResult {
        data class NextNode(val nodeId: String) : ResolutionResult()
        data object ChapterComplete : ResolutionResult()
        data class Error(val message: String) : ResolutionResult()
    }

    fun resolveNextNode(
        graph: ChapterGraph,
        currentNode: Node,
        handlerResult: String?
    ): ResolutionResult {
        // Handler explicitly returned next node
        if (handlerResult != null) {
            return ResolutionResult.NextNode(handlerResult)
        }

        // Chapter change node = end of current chapter
        if (currentNode is Node.ChapterChange) {
            return ResolutionResult.ChapterComplete
        }

        // Resolve via edges
        val nextEdges = getNextEdges(graph, currentNode.id)
        
        return when {
            nextEdges.isEmpty() -> ResolutionResult.ChapterComplete
            else -> ResolutionResult.NextNode(nextEdges.first().target)
        }
    }

    private fun getNextEdges(graph: ChapterGraph, nodeId: String): List<Edge> {
        return graph.edges.filter { it.source == nodeId }
    }
}
