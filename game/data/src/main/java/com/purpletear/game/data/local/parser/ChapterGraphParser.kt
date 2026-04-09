package com.purpletear.game.data.local.parser

import com.purpletear.game.data.local.dto.ChapterMetadataDto
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.Node

object ChapterGraphParser {

    fun parse(
        chapterCode: String,
        metadata: ChapterMetadataDto,
        nodeDtos: List<NodeDto>,
        edgeDtos: List<EdgeDto>
    ): ChapterGraph {
        val nodes = nodeDtos.mapNotNull { parseNode(it) }.associateBy { it.id }
        val edges = edgeDtos.map { parseEdge(it) }
        val startNodeId = nodes.values.filterIsInstance<Node.Start>().firstOrNull()?.id
            ?: nodes.keys.firstOrNull()
            ?: throw IllegalArgumentException("No start node found")

        return ChapterGraph(
            chapterCode = chapterCode,
            title = metadata.title,
            nodes = nodes,
            edges = edges,
            startNodeId = startNodeId
        )
    }

    private fun parseNode(dto: NodeDto): Node? {

        return when (dto.type) {
            "start" -> Node.Start(
                id = dto.id,
                label = dto.data.label ?: "Start"
            )

            "message" -> Node.Message(
                id = dto.id,
                text = dto.data.text ?: "",
                characterId = dto.data.characterId ?: -1,
                waitMs = dto.data.wait ?: 0,
                seenMs = dto.data.seen ?: 0
            )

            "chapter-change" -> Node.ChapterChange(
                id = dto.id,
                chapterCode = dto.data.chapterCode ?: ""
            )

            "scene-node" -> Node.Scene(
                id = dto.id,
                sceneId = dto.data.sceneId ?: 0
            )

            "condition" -> Node.Condition(
                id = dto.id,
                expression = dto.data.expression ?: "",
                trueTargetId = dto.data.trueTargetId ?: "",
                falseTargetId = dto.data.falseTargetId ?: ""
            )

            "memory" -> Node.Memory(
                id = dto.id,
                key = dto.data.key ?: "",
                value = dto.data.value ?: ""
            )

            "narration" -> Node.Info(
                id = dto.id,
                text = dto.data.text ?: ""
            )

            "trophy" -> Node.Trophy(
                id = dto.id,
                trophyId = dto.data.trophyId ?: ""
            )

            "signal" -> Node.Signal(
                id = dto.id,
                action = dto.data.action ?: "",
                payload = emptyMap()
            )

            "background" -> Node.Background(
                id = dto.id,
                imageUrl = dto.data.imageUrl ?: ""
            )

            "scene-node" -> Node.Scene(
                id = dto.id,
                sceneId = dto.data.sceneId ?: 0
            )

            else -> null
        }
    }

    private fun parseEdge(dto: EdgeDto): Edge {
        return Edge(
            source = dto.source,
            target = dto.target,
            type = parseEdgeType(dto.data?.edgeType),
            condition = dto.data?.condition
        )
    }

    private fun parseEdgeType(type: String?): EdgeType {
        return when (type?.uppercase()) {
            "CONDITIONAL" -> EdgeType.CONDITIONAL
            "CHOICE" -> EdgeType.CHOICE
            else -> EdgeType.NORMAL
        }
    }
}
