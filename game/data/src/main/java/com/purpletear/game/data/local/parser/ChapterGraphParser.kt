package com.purpletear.game.data.local.parser

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.purpletear.game.data.local.dto.ChapterMetadataDto
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.NodeDataDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeData
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.provider.GamePathProvider
import java.io.File

object ChapterGraphParser {
    private val gson = Gson()

    private fun JsonElement?.toNodeData(): NodeDataDto? {
        return when (this) {
            is JsonObject -> gson.fromJson(this, NodeDataDto::class.java)
            else -> null
        }
    }

    fun parse(
        chapterCode: String,
        metadata: ChapterMetadataDto,
        nodeDtos: List<NodeDto>,
        edgeDtos: List<EdgeDto>,
        gameId: String,
        pathProvider: GamePathProvider
    ): ChapterGraph {
        val nodes = nodeDtos.mapNotNull { parseNode(it, gameId, pathProvider) }.associateBy { it.id }
        val edges = edgeDtos.map { parseEdge(it) }
        val resolvedNodes = resolveConditionTargets(nodes, edges)
        val startNodeId = resolvedNodes.values.filterIsInstance<Node.Start>().firstOrNull()?.id
            ?: resolvedNodes.keys.firstOrNull()
            ?: throw IllegalArgumentException("No start node found")

        return ChapterGraph(
            chapterCode = chapterCode,
            title = metadata.title,
            nodes = resolvedNodes,
            edges = edges,
            startNodeId = startNodeId
        )
    }

    private fun parseNode(
        dto: NodeDto,
        gameId: String,
        pathProvider: GamePathProvider
    ): Node? {
        val data = dto.data.toNodeData()

        return when (dto.type) {
            "start" -> Node.Start(
                id = dto.id,
                label = data?.label ?: "Start"
            )

            "message" -> Node.Message(
                id = dto.id,
                text = data?.text ?: "",
                characterId = data?.characterId ?: -1,
                waitMs = data?.wait ?: 0,
                seenMs = data?.seen ?: 0,
                isHesitating = data?.isHesitating ?: false
            )

            "message-image" -> Node.MessageImage(
                id = dto.id,
                imageUrl = resolveImagePath(
                    data?.storagePath ?: data?.image ?: "",
                    gameId,
                    pathProvider
                ),
                characterId = data?.characterId ?: -1,
                waitMs = data?.wait ?: 0,
                seenMs = data?.seen ?: 0
            )

            "chapter-change" -> Node.ChapterChange(
                id = dto.id,
                chapterCode = data?.chapterCode ?: ""
            )

            "scene-node" -> Node.Scene(
                id = dto.id,
                sceneId = data?.sceneId ?: 0
            )

            "condition" -> Node.Condition(
                id = dto.id,
                expression = data?.expression ?: "",
                trueTargetId = data?.trueTargetId ?: "",
                falseTargetId = data?.falseTargetId ?: ""
            )

            "memory", "memory-save-node" -> Node.Memory(
                id = dto.id,
                key = data?.memory?.name ?: data?.key ?: "",
                value = data?.memory?.value ?: data?.value ?: ""
            )

            "memory-condition-node" -> Node.Condition(
                id = dto.id,
                expression = "${data?.memory?.name ?: ""} == ${data?.expectedValue ?: ""}",
                trueTargetId = "",
                falseTargetId = ""
            )

            "narration" -> Node.Info(
                id = dto.id,
                text = data?.text ?: ""
            )

            "trophy" -> Node.Trophy(
                id = dto.id,
                trophyId = data?.trophyId ?: ""
            )

            "signal" -> Node.Signal(
                id = dto.id,
                action = data?.action ?: "",
                payload = emptyMap()
            )

            "background" -> Node.Background(
                id = dto.id,
                imageUrl = data?.imageUrl ?: ""
            )

            "end" -> Node.End(
                id = dto.id
            )

            else -> null
        }
    }

    private fun resolveImagePath(
        storagePath: String,
        gameId: String,
        pathProvider: GamePathProvider
    ): String {
        if (storagePath.isBlank()) return ""
        val fileName = storagePath.substringAfterLast("/")
        val basePath = pathProvider.getStoryDirectoryPath(gameId)
        return "$basePath${File.separator}assets${File.separator}$fileName"
    }

    private fun parseEdge(dto: EdgeDto): Edge {
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

    /**
     * Resolves true/false target IDs for condition nodes from edges.
     * Condition nodes store their branching logic in outgoing edges with
     * edgeType "ConditionTrue" or "ConditionFalse".
     */
    private fun resolveConditionTargets(
        nodes: Map<String, Node>,
        edges: List<Edge>
    ): Map<String, Node> {
        // Build lookup: sourceNodeId -> (isTrueTarget -> targetNodeId)
        val conditionTargets = edges
            .filter { it.type == EdgeType.CONDITIONAL }
            .groupBy { it.source }
            .mapValues { (_, outgoingEdges) ->
                val targets = mutableMapOf<Boolean, String>()
                outgoingEdges.forEach { edge ->
                    when (edge.data?.edgeType?.uppercase()) {
                        "CONDITIONTRUE" -> targets[true] = edge.target
                        "CONDITIONFALSE" -> targets[false] = edge.target
                    }
                }
                targets
            }

        // Update condition nodes with resolved targets
        return nodes.mapValues { (nodeId, node) ->
            if (node is Node.Condition) {
                val targets = conditionTargets[nodeId]
                node.copy(
                    trueTargetId = targets?.get(true) ?: node.trueTargetId,
                    falseTargetId = targets?.get(false) ?: node.falseTargetId
                )
            } else {
                node
            }
        }
    }
}
