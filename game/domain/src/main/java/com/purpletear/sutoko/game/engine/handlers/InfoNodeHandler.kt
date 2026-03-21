package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for info nodes.
 *
 * Info nodes are silent nodes used for comments or metadata.
 * They produce no commands and simply pass through to the next node.
 */
class InfoNodeHandler @Inject constructor() : NodeHandler {
    override fun prepare(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        // Info nodes are silent - just pass through
        return HandlerScript()
    }
}
