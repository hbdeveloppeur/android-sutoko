package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEngineLogger
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for stop-sound nodes.
 *
 * Emits a StopSound effect to fade out and clear the sound started by the
 * targeted sound node. Clearing a sound that is not playing fails silently.
 */
class StopSoundNodeHandler @Inject constructor() : NodeHandler {
    override fun buildScript(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val stopSoundNode = node as? Node.StopSound ?: return HandlerScript()

        GameEngineLogger.d("HAND") { "StopSound ${stopSoundNode.id}: target=${stopSoundNode.targetNodeId}" }

        return HandlerScript(
            commands = listOf(
                HandlerCommand.Emit(
                    HandlerEffect.StopSound(targetNodeId = stopSoundNode.targetNodeId)
                )
            )
        )
    }
}
