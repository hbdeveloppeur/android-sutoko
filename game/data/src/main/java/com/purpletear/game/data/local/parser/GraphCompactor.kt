package com.purpletear.game.data.local.parser

import com.google.gson.JsonObject
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.NodeDto

/**
 * Removes transparent ("bypassed") nodes from an authored graph and reconnects edges so
 * the engine never has to execute them. A bypassed node is a no-op placeholder:
 * it emits nothing and is meant to be transparently skipped.
 *
 * A node is bypassed when:
 * - its type is "ignore", or
 * - it is a text-displaying node ("message", "narration", "intro-sentence") with a
 *   blank/missing text (tolerated authoring mistake: an empty text node displays
 *   nothing, so it is skipped rather than failing the whole chapter).
 *
 * Media nodes (message-image, message-vocal, sound, manga-page) stay strict: a missing
 * asset has no meaningful empty fallback.
 *
 * Transformation rules:
 * - Edges targeting a bypassed node are retargeted to the bypassed node's single
 *   outgoing target, keeping their own type/condition.
 * - Edges originating from a bypassed node are dropped (their purpose is already
 *   fulfilled by retargeting incoming edges).
 * - Chains of bypassed nodes are resolved transitively: the incoming edge lands on the
 *   first non-bypassed node. A cycle of bypassed nodes is a dead end.
 * - Bypassed nodes with zero or multiple outgoing edges are treated as dead ends:
 *   incoming edges to them are dropped.
 */
internal object GraphCompactor {

    fun compact(
        nodeDtos: List<NodeDto>,
        edgeDtos: List<EdgeDto>
    ): Pair<List<NodeDto>, List<EdgeDto>> {
        val bypassedNodeIds = nodeDtos
            .filter { it.isBypassed() }
            .map { it.id }
            .toSet()

        if (bypassedNodeIds.isEmpty()) {
            return nodeDtos to edgeDtos
        }

        val bypassTargets = bypassedNodeIds.associateWith { id ->
            resolveBypassTarget(id, bypassedNodeIds, edgeDtos)
        }

        val compactedEdges = edgeDtos.mapNotNull { edge ->
            when {
                edge.source in bypassedNodeIds -> null
                edge.target in bypassedNodeIds ->
                    bypassTargets[edge.target]?.let { edge.copy(target = it) }

                else -> edge
            }
        }

        val compactedNodes = nodeDtos.filter { it.id !in bypassedNodeIds }
        return compactedNodes to compactedEdges
    }

    /**
     * Walks the outgoing chain of a bypassed node until the first non-bypassed node.
     * Returns null on dead ends (zero/multiple outgoing edges) and on cycles.
     */
    private fun resolveBypassTarget(
        startId: String,
        bypassedNodeIds: Set<String>,
        edgeDtos: List<EdgeDto>
    ): String? {
        val visited = mutableSetOf<String>()
        var current = startId
        while (current in bypassedNodeIds && visited.add(current)) {
            val outgoing = edgeDtos.filter { it.source == current }
            if (outgoing.size != 1) return null
            current = outgoing.first().target
        }
        return current.takeIf { it !in bypassedNodeIds }
    }

    private fun NodeDto.isBypassed(): Boolean = when (type) {
        "ignore" -> true
        "message", "narration", "intro-sentence" -> textOf().isNullOrBlank()
        else -> false
    }

    private fun NodeDto.textOf(): String? {
        val element = (data as? JsonObject)?.get("text") ?: return null
        return if (element.isJsonPrimitive) element.asString else null
    }
}
