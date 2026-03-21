package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for start nodes.
 *
 * Start nodes are entry points that simply pass through to the next node
 * via graph edges. No commands are produced.
 */
class StartNodeHandler @Inject constructor() : NodeHandler {
    override fun prepare(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        // Start node is just an entry point - follow edges to next node
        return HandlerScript()
    }
}
