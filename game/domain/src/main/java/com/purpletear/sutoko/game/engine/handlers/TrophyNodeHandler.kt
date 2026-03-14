package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEvent
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

class TrophyNodeHandler @Inject constructor() : NodeHandler {
    override suspend fun handle(
        node: Node,
        memory: GameMemory,
        emit: (GameEvent) -> Unit
    ): String? {
        val trophyNode = node as? Node.Trophy ?: return null

        memory.set("trophy_${trophyNode.trophyId}", "unlocked")
        emit(GameEvent.SendSignal("trophy_unlocked", mapOf("trophyId" to trophyNode.trophyId)))

        return null // Let engine find next via edges
    }
}
