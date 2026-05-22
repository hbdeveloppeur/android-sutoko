package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for sound nodes.
 *
 * Emits a PlaySound effect to play a sound effect or ambient audio.
 * Supports looping via the node's loop flag.
 */
class SoundNodeHandler @Inject constructor() : NodeHandler {
    override fun buildScript(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val soundNode = node as? Node.Sound ?: return HandlerScript()

        return HandlerScript(
            commands = listOf(
                HandlerCommand.Emit(
                    HandlerEffect.PlaySound(
                        soundUrl = soundNode.soundUrl,
                        loop = soundNode.loop
                    )
                )
            )
        )
    }
}
