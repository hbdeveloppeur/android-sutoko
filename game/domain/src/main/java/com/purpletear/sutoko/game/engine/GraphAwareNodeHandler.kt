package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node

/**
 * Marker interface for handlers that need the full [ChapterGraph] to build
 * their script. The engine dispatches to the 3-argument version only for
 * handlers implementing this interface, keeping the simpler [NodeHandler]
 * contract unchanged for everyone else.
 */
interface GraphAwareNodeHandler : NodeHandler {

    /**
     * Default implementation satisfies [NodeHandler] but should never be used.
     * Callers must use [buildScript] with the graph argument.
     */
    override fun buildScript(node: Node, memory: GameMemory): HandlerScript {
        throw UnsupportedOperationException(
            "${this::class.simpleName} requires a ChapterGraph; use the 3-argument buildScript"
        )
    }

    fun buildScript(
        node: Node,
        memory: GameMemory,
        graph: ChapterGraph
    ): HandlerScript
}
