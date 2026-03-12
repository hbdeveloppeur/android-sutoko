package com.purpletear.sutoko.game.model.chapter

import androidx.annotation.Keep

@Keep
data class ChapterGraph(
    val chapterCode: String,
    val title: String,
    val nodes: Map<String, Node>,
    val edges: List<Edge>,
    val startNodeId: String
) {
    fun getNode(id: String): Node? = nodes[id]

    fun getNextEdges(nodeId: String): List<Edge> = edges.filter { it.source == nodeId }

    fun getNextNode(currentNodeId: String, choiceIndex: Int = 0): String? {
        val nextEdges = getNextEdges(currentNodeId)
        return when {
            nextEdges.isEmpty() -> null
            choiceIndex < nextEdges.size -> nextEdges[choiceIndex].target
            else -> nextEdges.firstOrNull()?.target
        }
    }
}
