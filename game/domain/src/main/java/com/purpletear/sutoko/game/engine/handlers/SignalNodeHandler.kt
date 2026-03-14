package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEvent
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

class SignalNodeHandler @Inject constructor() : NodeHandler {
    override suspend fun handle(
        node: Node,
        memory: GameMemory,
        emit: (GameEvent) -> Unit
    ): String? {
        val signalNode = node as? Node.Signal ?: return null

        emit(
            GameEvent.SendSignal(
                action = signalNode.action,
                payload = signalNode.payload
            )
        )

        return null // Let engine find next via edges
    }
}
