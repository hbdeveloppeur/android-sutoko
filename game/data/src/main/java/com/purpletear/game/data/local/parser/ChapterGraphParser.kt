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
        val nodes =
            nodeDtos.mapNotNull { parseNode(it, gameId, pathProvider) }.associateBy { it.id }
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
                text = requireNotNull(data?.text) { "message node ${dto.id} missing text" },
                characterId = data?.characterId ?: -1,
                waitMs = data?.wait ?: 0,
                seenMs = data?.seen ?: 0,
                isHesitating = data?.isHesitating ?: false
            )

            "message-image" -> {
                val imagePath = data?.storagePath ?: data?.image
                require(imagePath != null) { "message-image node ${dto.id} missing storagePath or image" }
                Node.MessageImage(
                    id = dto.id,
                    imageUrl = resolveImagePath(imagePath, gameId, pathProvider),
                    characterId = data?.characterId ?: -1,
                    waitMs = data?.wait ?: 0,
                    seenMs = data?.seen ?: 0
                )
            }

            "chapter-change" -> {
                val id = dto.id
                val chapterCode = data?.chapterCode
                require(id.isNotBlank()) { "chapter-change node missing id" }
                require(!chapterCode.isNullOrBlank()) { "chapter-change node $id missing chapterCode" }
                Node.ChapterChange(
                    id = id,
                    chapterCode = chapterCode
                )
            }

            "scene-node" -> Node.Scene(
                id = dto.id,
                sceneId = requireNotNull(data?.sceneId) { "scene-node ${dto.id} missing sceneId" }
            )

            "condition" -> Node.Condition(
                id = dto.id,
                expression = requireNotNull(data?.expression) { "condition node ${dto.id} missing expression" },
                trueTargetId = requireNotNull(data?.trueTargetId) { "condition node ${dto.id} missing trueTargetId" },
                falseTargetId = requireNotNull(data?.falseTargetId) { "condition node ${dto.id} missing falseTargetId" }
            )

            "memory", "memory-save-node" -> {
                val id = dto.id
                val key = data?.memory?.name ?: data?.key
                val value = data?.memory?.value ?: data?.value
                require(id.isNotBlank()) { "memory node missing id" }
                require(!key.isNullOrBlank()) { "memory node $id missing key" }
                require(!value.isNullOrBlank()) { "memory node $id missing value" }
                Node.Memory(
                    id = id,
                    key = key,
                    value = value
                )
            }

            "memory-condition-node" -> {
                val key = data?.memory?.name ?: data?.key
                require(!key.isNullOrBlank()) { "memory-condition-node ${dto.id} missing key" }
                val expectedValue =
                    requireNotNull(data?.expectedValue) { "memory-condition-node ${dto.id} missing expectedValue" }
                Node.Condition(
                    id = dto.id,
                    expression = "$key == $expectedValue",
                    trueTargetId = requireNotNull(data?.trueTargetId) { "memory-condition-node ${dto.id} missing trueTargetId" },
                    falseTargetId = requireNotNull(data?.falseTargetId) { "memory-condition-node ${dto.id} missing falseTargetId" }
                )
            }

            "narration" -> {
                val id = dto.id
                val text = data?.text
                require(id.isNotBlank()) { "narration node missing id" }
                require(!text.isNullOrBlank()) { "narration node $id missing text" }
                Node.Info(
                    id = id, text = text,
                )
            }

            "trophy" -> Node.Trophy(
                id = dto.id,
                trophyId = requireNotNull(data?.trophyId) { "trophy node ${dto.id} missing trophyId" }
            )

            "background" -> Node.Background(
                id = dto.id,
                imageUrl = data?.imageUrl ?: ""
            )

            "end" -> Node.End(
                id = dto.id
            )

            "sound" -> {
                val storagePath =
                    requireNotNull(data?.storagePath) { "sound node ${dto.id} missing storagePath" }
                val id = dto.id
                val loop = data.isLooping ?: false
                Node.Sound(
                    id = id,
                    soundUrl = resolveSoundPath(storagePath, gameId, pathProvider),
                    loop = loop
                )
            }

            "message-vocal" -> {
                val storagePath =
                    requireNotNull(data?.storagePath) { "message-vocal node ${dto.id} missing storagePath" }
                val characterId =
                    requireNotNull(data.characterId) { "message-vocal node ${dto.id} missing characterId" }

                Node.MessageVocal(
                    id = dto.id,
                    audioUrl = resolveSoundPath(storagePath, gameId, pathProvider),
                    characterId = characterId
                )
            }

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

    private fun resolveSoundPath(
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
