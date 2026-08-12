package com.purpletear.game.data.local.parser

import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeData
import com.purpletear.sutoko.game.model.chapter.EdgeType

internal fun parseEdge(dto: EdgeDto): Edge {
    return Edge(
        source = dto.source,
        target = dto.target,
        type = parseEdgeType(dto.data?.edgeType),
        condition = dto.data?.condition,
        data = dto.data?.let { EdgeData(edgeType = it.edgeType) }
    )
}

private fun parseEdgeType(type: String?): EdgeType {
    return when (type?.uppercase()) {
        "CONDITIONAL", "CONDITIONTRUE", "CONDITIONFALSE" -> EdgeType.CONDITIONAL
        "CHOICE" -> EdgeType.CHOICE
        else -> EdgeType.NORMAL
    }
}
