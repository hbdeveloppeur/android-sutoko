package com.purpletear.game.presentation.smsgame.engine.handlers

import com.purpletear.game.presentation.smsgame.engine.GameEvent
import com.purpletear.game.presentation.smsgame.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

class StartNodeHandler @Inject constructor() : NodeHandler {
    override suspend fun handle(
        node: Node,
        memory: GameMemory,
        emit: (GameEvent) -> Unit
    ): String? {
        val startNode = node as? Node.Start ?: return null
        return getNextNodeId(node.id, memory)
    }

    private fun getNextNodeId(currentId: String, memory: GameMemory): String? {
        // This will be resolved by the GameEngine based on edges
        // For now, return null to let the engine handle it
        return null
    }
}
