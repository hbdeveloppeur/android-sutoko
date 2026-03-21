package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for signal nodes.
 *
 * Emits a SendSignal effect for external system communication
 * (analytics, telemetry, remote logging, etc.).
 */
class SignalNodeHandler @Inject constructor() : NodeHandler {
    override fun prepare(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val signalNode = node as? Node.Signal ?: return HandlerScript()

        return HandlerScript(
            commands = listOf(
                HandlerCommand.Emit(
                    HandlerEffect.SendSignal(
                        action = signalNode.action,
                        payload = signalNode.payload
                    )
                )
            )
        )
    }
}
