package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for trophy/achievement nodes.
 *
 * Emits an UnlockTrophy effect to notify the presentation layer
 * that a trophy should be unlocked. The actual trophy persistence
 * is handled by the presentation layer.
 */
class TrophyNodeHandler @Inject constructor() : NodeHandler {
    override fun prepare(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val trophyNode = node as? Node.Trophy ?: return HandlerScript()

        return HandlerScript(
            commands = listOf(
                HandlerCommand.Emit(HandlerEffect.UnlockTrophy(trophyNode.trophyId))
            )
        )
    }
}
