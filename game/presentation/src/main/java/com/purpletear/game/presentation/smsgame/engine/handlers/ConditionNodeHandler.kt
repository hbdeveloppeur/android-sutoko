package com.purpletear.game.presentation.smsgame.engine.handlers

import com.purpletear.game.presentation.smsgame.engine.GameEvent
import com.purpletear.game.presentation.smsgame.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

class ConditionNodeHandler @Inject constructor() : NodeHandler {
    override suspend fun handle(
        node: Node,
        memory: GameMemory,
        emit: (GameEvent) -> Unit
    ): String? {
        val conditionNode = node as? Node.Condition ?: return null

        val isTrue = memory.evaluateCondition(conditionNode.expression)

        return if (isTrue) {
            conditionNode.trueTargetId
        } else {
            conditionNode.falseTargetId
        }
    }
}
