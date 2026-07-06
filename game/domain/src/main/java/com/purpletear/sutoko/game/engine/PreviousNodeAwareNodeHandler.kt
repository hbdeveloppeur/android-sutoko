package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node

/**
 * Marker interface for handlers that need the previously executed node to build
 * their script. The engine dispatches to the 3-argument version only for
 * handlers implementing this interface, keeping the simpler [NodeHandler]
 * contract unchanged for everyone else.
 *
 * A handler cannot implement both [GraphAwareNodeHandler] and [PreviousNodeAwareNodeHandler];
 * the engine treats that as an error. If a handler needs both the graph and the previous node,
 * introduce a combined interface and dispatch for it in [GameEngine.buildScript].
 */
interface PreviousNodeAwareNodeHandler : NodeHandler {

    /**
     * Builds a script using the previously executed node as context.
     *
     * @param node The node to execute
     * @param memory The game memory for read-only state inspection
     * @param previousNode The node executed immediately before this one, or null
     *                     if this is the first node of the session/chapter
     * @param arrivalContext Transient information about how the node was reached
     */
    fun buildScript(
        node: Node,
        memory: GameMemory,
        previousNode: Node?,
        arrivalContext: ArrivalContext = ArrivalContext(),
    ): HandlerScript
}
