package com.purpletear.game.presentation.smsgame.engine.handlers

import com.purpletear.game.presentation.smsgame.engine.GameEvent
import com.purpletear.game.presentation.smsgame.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

class InfoNodeHandler @Inject constructor() : NodeHandler {
    override suspend fun handle(
        node: Node,
        memory: GameMemory,
        emit: (GameEvent) -> Unit
    ): String? {
        val infoNode = node as? Node.Info ?: return null

        emit(GameEvent.ShowInfo(infoNode.text))

        return null // Let engine find next via edges
    }
}
