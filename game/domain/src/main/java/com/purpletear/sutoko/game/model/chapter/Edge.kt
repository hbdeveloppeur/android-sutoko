package com.purpletear.sutoko.game.model.chapter

import androidx.annotation.Keep

@Keep
data class Edge(
    val source: String,
    val target: String,
    val type: EdgeType = EdgeType.NORMAL,
    val condition: String? = null
)

@Keep
enum class EdgeType {
    NORMAL,
    CONDITIONAL,
    CHOICE
}
