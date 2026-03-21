package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for condition/decision nodes.
 *
 * Evaluates a conditional expression against game memory and returns
 * the appropriate next node. No commands are produced - this is pure
 * navigation logic.
 *
 * Example expressions:
 * - "variableName == value"
 * - "score >= 100"
 * - "hasKey == true && doorUnlocked == false"
 */
class ConditionNodeHandler @Inject constructor() : NodeHandler {
    override fun prepare(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val conditionNode = node as? Node.Condition ?: return HandlerScript()

        val isTrue = memory.evaluateCondition(conditionNode.expression)

        val nextNodeId = if (isTrue) {
            conditionNode.trueTargetId
        } else {
            conditionNode.falseTargetId
        }

        return HandlerScript(nextNodeId = nextNodeId)
    }
}
