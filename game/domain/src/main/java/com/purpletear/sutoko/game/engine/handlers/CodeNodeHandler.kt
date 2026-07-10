package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEngineLogger
import com.purpletear.sutoko.game.engine.GraphAwareNodeHandler
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for `code` nodes.
 *
 * Recognises the `[intro=start]` marker and emits [HandlerEffect.EnterCinematic] carrying the
 * matching `[intro=end]` marker id, so the presentation layer can extract and play the cinematic
 * body. The engine parks itself while applying that effect (see `GameEngine.applyEffect`), so this
 * handler does not navigate forward and the body is never executed as SMS.
 *
 * Any other code sentence is ignored (reserved for future codes) and traversal continues to the
 * node's successor.
 */
class CodeNodeHandler @Inject constructor() : GraphAwareNodeHandler {

    override fun buildScript(
        node: Node,
        memory: GameMemory,
        graph: ChapterGraph
    ): HandlerScript {
        val code = node as? Node.Code ?: return HandlerScript()
        if (!code.isIntroStart) return HandlerScript()

        val endNodeId = findIntroEnd(code.id, graph)
        if (endNodeId == null) {
            GameEngineLogger.e("CODE") {
                "[intro=start] at ${code.id} has no matching [intro=end] — skipping cinematic"
            }
            return HandlerScript()
        }

        GameEngineLogger.d("CODE") { "EnterCinematic ${code.id} → $endNodeId" }
        return HandlerScript(
            commands = listOf(
                HandlerCommand.Emit(
                    HandlerEffect.EnterCinematic(
                        startNodeId = code.id,
                        endNodeId = endNodeId
                    )
                )
            )
        )
    }

    private fun findIntroEnd(startId: String, graph: ChapterGraph): String? {
        val visited = HashSet<String>()
        var current: String? = graph.singleSuccessor(startId)
        while (current != null) {
            if (!visited.add(current)) return null
            val node = graph.getNode(current) ?: return null
            if (node is Node.Code && node.isIntroEnd) return current
            current = graph.singleSuccessor(current) ?: return null
        }
        return null
    }
}
