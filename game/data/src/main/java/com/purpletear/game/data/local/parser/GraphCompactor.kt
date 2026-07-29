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
