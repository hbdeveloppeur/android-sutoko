package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for background nodes.
 *
 * Emits a ChangeBackground effect to update the scene background.
 */
class BackgroundNodeHandler @Inject constructor() : NodeHandler {
    override fun prepare(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val backgroundNode = node as? Node.Background ?: return HandlerScript()

        return HandlerScript(
            commands = listOf(
                HandlerCommand.Emit(HandlerEffect.ChangeBackground(backgroundNode.imageUrl))
            )
        )
    }
}
