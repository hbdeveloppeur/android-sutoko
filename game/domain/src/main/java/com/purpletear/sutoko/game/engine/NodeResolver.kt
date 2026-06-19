package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject
import androidx.annotation.Keep

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
        data class Error(val message: String) : ResolutionResult()
    }

    fun resolveNextNode(
        graph: ChapterGraph,
        currentNode: Node,
        forceNodId: String?
    ): ResolutionResult {
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
            else -> {
                val target = nextEdges.first().target
                assert(target.isNotBlank())
                ResolutionResult.NextNode(target)
            }
        }
    }

}
