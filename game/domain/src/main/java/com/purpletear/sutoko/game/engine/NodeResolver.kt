package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handles all node navigation logic.
 * Separated from GameEngine for testability.
 */
class NodeResolver @Inject constructor() {

    sealed class ResolutionResult {
        data class NextNode(val nodeId: String) : ResolutionResult()
        data object NodeNextChapter : ResolutionResult()
        data class Error(val message: String) : ResolutionResult()
    }

    fun resolveNextNode(
        graph: ChapterGraph,
        currentNode: Node,
        forceNodId: String?
    ): ResolutionResult {
        // Handler explicitly returned next node
        if (forceNodId != null) {
            return ResolutionResult.NextNode(forceNodId)
        }

        // Chapter change node = end of current chapter
        if (currentNode is Node.ChapterChange) {
            return ResolutionResult.NodeNextChapter
        }

        // Resolve via edges
        val nextEdges = graph.getNextEdges(currentNode.id)

        return when {
            // TODO: when empty, throw. Bad state
            nextEdges.isEmpty() -> ResolutionResult.NodeNextChapter
            else -> ResolutionResult.NextNode(nextEdges.first().target)
        }
    }

}
