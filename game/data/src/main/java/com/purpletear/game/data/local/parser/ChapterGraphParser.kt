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
    private val AUDIO_EXTENSIONS = listOf("mp3", "ogg", "wav")

    private fun JsonElement?.toNodeData(): NodeDataDto? {
        return when (this) {
            is JsonObject -> gson.fromJson(this, NodeDataDto::class.java)
            else -> null
        }
    }

    fun parse(
        chapterCode: String,
        chapterNumber: Int = 1,
        metadata: ChapterMetadataDto,
        nodeDtos: List<NodeDto>,
        edgeDtos: List<EdgeDto>,
        gameId: String,
        legacyId: Int?,
        pathProvider: GamePathProvider
    ): ChapterGraph {
        val (compactedNodeDtos, compactedEdgeDtos) =
            compactIgnoreNodes(nodeDtos, edgeDtos)

        val nodes = compactedNodeDtos
            .mapNotNull { parseNode(it, gameId, legacyId, pathProvider) }
            .associateBy { it.id }
        val edges = compactedEdgeDtos.map { parseEdge(it) }
        val startNodeId = nodes.values.filterIsInstance<Node.Start>().firstOrNull()?.id
            ?: nodes.keys.firstOrNull()
            ?: throw IllegalArgumentException("No start node found")

        return ChapterGraph(
            chapterCode = chapterCode.uppercase(),
            chapterNumber = chapterNumber,
            title = metadata.title,
            nodes = nodes,
            edges = edges,
            startNodeId = startNodeId
        )
    }

    /**
     * Removes "ignore" nodes from the authored graph and reconnects edges so the
     * engine never has to execute them. An ignore node is a no-op placeholder:
     * it emits nothing and is meant to be transparently bypassed.
     *
     * Transformation rules:
     * - Edges targeting an ignore node are retargeted to the ignore node's single
     *   outgoing target.
     * - Edges originating from an ignore node are dropped (their purpose is already
     *   fulfilled by retargeting incoming edges).
     * - Ignore nodes with zero or multiple outgoing edges are treated as dead ends:
     *   incoming edges to them are dropped.
     */
    private fun compactIgnoreNodes(
        nodeDtos: List<NodeDto>,
        edgeDtos: List<EdgeDto>
    ): Pair<List<NodeDto>, List<EdgeDto>> {
        val ignoreNodeIds = nodeDtos
            .filter { it.type == "ignore" }
            .map { it.id }
            .toSet()

        if (ignoreNodeIds.isEmpty()) {
            return nodeDtos to edgeDtos
        }

        val ignoreBypassTargets = ignoreNodeIds.associateWith { ignoreId ->
            val outgoing = edgeDtos.filter { it.source == ignoreId }
            when (outgoing.size) {
                1 -> outgoing.first().target
                else -> null
            }
        }

        val compactedEdges = edgeDtos.mapNotNull { edge ->
            when {
                edge.source in ignoreNodeIds -> null
                edge.target in ignoreNodeIds -> {
                    val bypassTarget = ignoreBypassTargets[edge.target]
                    if (bypassTarget != null) {
                        edge.copy(target = bypassTarget, type = edge.type)
                    } else {
                        null
                    }
                }

                else -> edge
            }
        }

        val compactedNodes = nodeDtos.filter { it.type != "ignore" }

        return compactedNodes to compactedEdges
    }

    private fun parseNode(
        dto: NodeDto,
        gameId: String,
        legacyId: Int?,
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
                    imageUrl = resolveImagePath(imagePath, gameId, legacyId, pathProvider),
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
                expression = requireNotNull(data?.expression) { "condition node ${dto.id} missing expression" }
            )

            "memory", "memory-save-node" -> {
                val id = dto.id
                require(data?.memory != null) { "memory node missing key memory" }
                val memory = data.memory
                Node.Memory(
                    id = id,
                    key = memory.key,
                    value = memory.value
                )
            }

            "memory-condition-node" -> {
                require(data?.memory != null) { "memory-condition-node ${dto.id} memory is null" }
                val memory = data.memory

                val expectedValue =
                    requireNotNull(data.expectedValue) { "memory-condition-node ${dto.id} missing expectedValue" }
                Node.Condition(
                    id = dto.id,
                    expression = "${memory.key} == $expectedValue"
                )
            }

            "narration" -> {
                val id = dto.id
                val text = data?.text
                require(id.isNotBlank()) { "narration node missing id" }
                require(!text.isNullOrBlank()) { "narration node $id missing text" }
                Node.Info(
                    id = id, text = text
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
                    soundUrl = resolveSoundPath(storagePath, gameId, legacyId, pathProvider),
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
                    audioUrl = resolveSoundPath(storagePath, gameId, legacyId, pathProvider),
                    characterId = characterId
                )
            }

            else -> null
        }
    }

    private fun resolveImagePath(
        storagePath: String,
        gameId: String,
        legacyId: Int?,
        pathProvider: GamePathProvider
    ): String {
        if (storagePath.isBlank()) return ""
        val fileName = storagePath.substringAfterLast("/")
        val basePath = pathProvider.getStoryDirectoryPath(gameId, legacyId)
        return "$basePath${File.separator}assets${File.separator}$fileName"
    }

    private fun resolveSoundPath(
        storagePath: String,
        gameId: String,
        legacyId: Int?,
        pathProvider: GamePathProvider
    ): String {
        val assetName = storagePath.substringAfterLast("/")
        if (assetName.isBlank()) return ""

        val storyPath = pathProvider.getStoryDirectoryPath(gameId, legacyId)
        val primary = "$storyPath${File.separator}assets${File.separator}$assetName"

        // Legacy archives placed sound files under medias/sounds/.
        // Try the new location first, then the legacy one.
        val candidates = listOf(
            primary,
            "$storyPath${File.separator}medias${File.separator}sounds${File.separator}$assetName"
        )

        val extensionCandidates = if ("." !in assetName) {
            candidates.flatMap { base ->
                AUDIO_EXTENSIONS.map { "$base.$it" }
            }
        } else {
            emptyList()
        }

        return (candidates + extensionCandidates).firstOrNull { File(it).exists() }
            ?: primary
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

}
