package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for memory nodes.
 *
 * Updates game memory variables. Instead of mutating GameMemory directly,
 * emits an UpdateMemory effect that the engine applies. This ensures
 * all state changes flow through the engine and are auditable.
 */
class MemoryNodeHandler @Inject constructor() : NodeHandler {
    override fun prepare(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val memoryNode = node as? Node.Memory ?: return HandlerScript()

        // Emit effect instead of direct mutation
        // This keeps all state changes explicit and auditable
        return HandlerScript(
            commands = listOf(
                HandlerCommand.Emit(
                    HandlerEffect.UpdateMemory(
                        key = memoryNode.key,
                        value = memoryNode.value
                    )
                )
            )
        )
    }
}
