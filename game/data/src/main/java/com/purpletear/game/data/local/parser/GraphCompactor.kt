package com.purpletear.game.data.local.parser

import com.google.gson.JsonObject
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.NodeDto

internal object GraphCompactor {

    fun compact(
        nodeDtos: List<NodeDto>,
        edgeDtos: List<EdgeDto>
    ): Pair<List<NodeDto>, List<EdgeDto>> {
        val bypassedNodeIds = nodeDtos
            .filter { it.isBypassed(edgeDtos) }
            .map { it.id }
            .toSet()

        if (bypassedNodeIds.isEmpty()) {
            return nodeDtos to edgeDtos
        }

        // Only ignored nodes keep the "fan-out drops incoming edges" semantics;
        // blank narrations/intro-sentences with fan-out are spliced instead so
        // the choice hub they feed stays reachable.
        val splicedFanOutIds = nodeDtos
            .filter { it.id in bypassedNodeIds && it.type != "ignore" }
            .map { it.id }
            .toSet()

        val bypassTargets = bypassedNodeIds.associateWith { id ->
            resolveBypassTargets(id, bypassedNodeIds, splicedFanOutIds, edgeDtos)
        }

        val compactedEdges = edgeDtos.flatMap { edge ->
            when {
                edge.source in bypassedNodeIds -> emptyList()
                edge.target in bypassedNodeIds ->
                    bypassTargets[edge.target].orEmpty().map { edge.copy(target = it) }

                else -> listOf(edge)
            }
        }

        val compactedNodes = nodeDtos.filter { it.id !in bypassedNodeIds }
        return compactedNodes to compactedEdges
    }

    /**
     * Walks the outgoing chains of a bypassed node until every path reaches a
     * non-bypassed node. A bypassed node that fans out (e.g. an empty narration
     * feeding a choice hub) is spliced: each incoming edge is retargeted to all
     * of its reachable successors. Fan-out on nodes outside [splicedFanOutIds]
     * (ignored nodes) resolves to null. Returns null when no successor is
     * reachable (dead end) and skips cycles.
     */
    private fun resolveBypassTargets(
        startId: String,
        bypassedNodeIds: Set<String>,
        splicedFanOutIds: Set<String>,
        edgeDtos: List<EdgeDto>
    ): List<String>? {
        val visited = mutableSetOf<String>()
        val targets = mutableListOf<String>()
        val seenTargets = mutableSetOf<String>()
        val queue = ArrayDeque(listOf(startId))
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current !in bypassedNodeIds) {
                if (seenTargets.add(current)) targets.add(current)
                continue
            }
            if (!visited.add(current)) continue
            val outgoing = edgeDtos.filter { it.source == current }
            when (outgoing.size) {
                0 -> return null
                1 -> queue.add(outgoing.first().target)
                else -> {
                    if (current !in splicedFanOutIds) return null
                    outgoing.forEach { queue.add(it.target) }
                }
            }
        }
        return targets.takeIf { it.isNotEmpty() }
    }

    private fun NodeDto.isBypassed(edgeDtos: List<EdgeDto>): Boolean = when (type) {
        "ignore" -> true
        "message" -> textOf().isNullOrBlank() && !fansOutToChoices(edgeDtos)
        "narration", "intro-sentence" -> textOf().isNullOrBlank()
        else -> false
    }

    /** A blank message with several outgoing edges is a choice hub, not a dead end. */
    private fun NodeDto.fansOutToChoices(edgeDtos: List<EdgeDto>): Boolean =
        edgeDtos.count { it.source == id } > 1

    private fun NodeDto.textOf(): String? {
        val element = (data as? JsonObject)?.get("text") ?: return null
        return if (element.isJsonPrimitive) element.asString else null
    }
}
