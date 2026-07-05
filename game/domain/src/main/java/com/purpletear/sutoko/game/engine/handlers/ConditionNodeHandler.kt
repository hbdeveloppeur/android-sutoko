package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEngineLogger
import com.purpletear.sutoko.game.engine.GraphAwareNodeHandler
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for condition/decision nodes.
 *
 * Evaluates a conditional expression against game memory and returns
 * the appropriate next node by inspecting the outgoing conditional edges.
 * No commands are produced - this is pure navigation logic.
 *
 * Example expressions:
 * - "variableName == value"
 * - "score >= 100"
 * - "hasKey == true && doorUnlocked == false"
 */
class ConditionNodeHandler @Inject constructor() : GraphAwareNodeHandler {

    override fun buildScript(
        node: Node,
        memory: GameMemory,
        graph: ChapterGraph
    ): HandlerScript {
        val conditionNode = node as? Node.Condition ?: return HandlerScript()

        val isTrue = memory.evaluateCondition(conditionNode.expression)
        val nextNodeId = resolveNextNodeId(conditionNode.id, isTrue, graph)

        GameEngineLogger.d("COND") {
            "\"${conditionNode.expression}\" = $isTrue → ${nextNodeId ?: "<no branch>"}"
        }

        return if (nextNodeId != null) {
            HandlerScript(nextNodeId = nextNodeId)
        } else {
            HandlerScript()
        }
    }

    private fun resolveNextNodeId(
        nodeId: String,
        isTrue: Boolean,
        graph: ChapterGraph
    ): String? {
        val branchEdges = graph.getNextEdges(nodeId).filter { it.type == EdgeType.CONDITIONAL }
        if (branchEdges.isEmpty()) {
            GameEngineLogger.e("COND") { "Node $nodeId has no conditional edges" }
            return null
        }

        val expectedEdgeType = if (isTrue) "CONDITIONTRUE" else "CONDITIONFALSE"
        val branch = branchEdges.find { it.data?.edgeType?.uppercase() == expectedEdgeType }
        if (branch == null) {
            GameEngineLogger.e("COND") {
                "Node $nodeId missing $expectedEdgeType edge among ${branchEdges.map { it.data?.edgeType }}"
            }
            return null
        }

        return branch.target
    }
}
