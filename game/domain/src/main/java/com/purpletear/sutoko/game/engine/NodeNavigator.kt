package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handles all node navigation logic.
 * Separated from GameEngine for testability.
 */
class NodeNavigator @Inject constructor() {

    sealed class NavigationResult {
        data class NextNode(val nodeId: String) : NavigationResult()
        data object WaitingForInput : NavigationResult()
        data object ChapterComplete : NavigationResult()
        data class Error(val message: String) : NavigationResult()
    }

    fun resolveNextNode(
        graph: ChapterGraph,
        currentNodeId: String,
        currentNode: Node,
        handlerResult: String?
    ): NavigationResult {
        // Handler explicitly returned next node
        if (handlerResult != null) {
            return NavigationResult.NextNode(handlerResult)
        }

        // Choice nodes always wait for user input
        if (currentNode is Node.Choice) {
            return NavigationResult.WaitingForInput
        }

        // Chapter change node = end of current chapter
        if (currentNode is Node.ChapterChange) {
            return NavigationResult.ChapterComplete
        }

        // Resolve via edges
        val nextEdges = getNextEdges(graph, currentNodeId)
        
        return when {
            nextEdges.isEmpty() -> NavigationResult.ChapterComplete
            else -> NavigationResult.NextNode(nextEdges.first().target)
        }
    }

    private fun getNextEdges(graph: ChapterGraph, nodeId: String): List<Edge> {
        return graph.edges.filter { it.source == nodeId }
    }
}
