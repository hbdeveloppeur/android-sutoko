package com.purpletear.game.data.local.parser

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.purpletear.game.data.local.dto.ChapterMetadataDto
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.MangaMessageDto
import com.purpletear.game.data.local.dto.NodeDataDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeData
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.IntroAlignment
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.provider.GamePathProvider
import java.io.File

object ChapterGraphParser {
    private val gson = Gson()
    private val AUDIO_EXTENSIONS = listOf("mp3", "ogg", "wav")

    // Manga page: bounds and legacy-compatible defaults (see legacy MangaHelper.parseMessage).
    private const val MAX_MANGA_MESSAGES = 32
    private const val MAX_MANGA_SENTENCE_LEN = 500
    private const val DEFAULT_MANGA_SIZE = 30f
    private const val DEFAULT_MANGA_X = 1f
    private const val DEFAULT_MANGA_Y = 1f
    private const val DEFAULT_MANGA_W = 10f

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
            GraphCompactor.compact(nodeDtos, edgeDtos)

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
                // Blank messages are bypassed upstream by GraphCompactor; this require is an invariant guard.
                text = requireNotNull(data?.text) { "message node ${dto.id} missing text" },
                characterId = data?.characterId ?: -1,
                waitMs = data?.wait ?: 0,
                seenMs = data?.seen ?: 0,
                isHesitating = data?.isHesitating ?: false
            )

            "message-theme" -> Node.MessageTheme(
                id = dto.id,
                backgroundColor = data?.backgroundColor?.trim()?.takeIf { it.isNotEmpty() },
                foregroundColor = data?.foregroundColor?.trim()?.takeIf { it.isNotEmpty() }
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
                // Blank narrations are bypassed upstream by GraphCompactor; this require is an invariant guard.
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

            "code-message" -> Node.Code(
                id = dto.id,
                sentence = data?.text?.trim().orEmpty()
            )

            "intro-sentence" -> Node.IntroSentence(
                id = dto.id,
                // Blank intro-sentences are bypassed upstream by GraphCompactor; invariant guard.
                text = requireNotNull(data?.text?.takeIf { it.isNotBlank() }) {
                    "intro-sentence node ${dto.id} missing text"
                },
                alignment = parseIntroAlignment(data?.alignment, dto.id),
                delayMs = data?.delay ?: 0,
                durationMs = data?.duration ?: 0
            )

            "manga-page" -> parseMangaPage(dto, data, gameId, legacyId, pathProvider)

            else -> null
        }
    }

    private fun parseIntroAlignment(raw: String?, nodeId: String): IntroAlignment {
        if (raw.isNullOrBlank()) return IntroAlignment.CENTER
        return when (raw.trim().lowercase()) {
            "start" -> IntroAlignment.START
            "end" -> IntroAlignment.END
            "top" -> IntroAlignment.TOP
            "bottom" -> IntroAlignment.BOTTOM
            "center" -> IntroAlignment.CENTER
            else -> throw IllegalArgumentException(
                "intro-sentence node $nodeId has unknown alignment '$raw' " +
                        "(expected start|end|top|bottom|center)"
            )
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

    private fun parseMangaPage(
        dto: NodeDto,
        data: NodeDataDto?,
        gameId: String,
        legacyId: Int?,
        pathProvider: GamePathProvider
    ): Node? {
        val id = dto.id
        val fileName =
            data?.storagePath?.trim()?.takeIf { it.isNotEmpty() }?.substringAfterLast("/")
        require(fileName != null) { "manga-page node $id missing assetFileName" }

        // Bound parsing work and drop malformed entries rather than crashing.
        val messages = data.messages.orEmpty()
            .take(MAX_MANGA_MESSAGES)
            .mapNotNull { it.toMangaMessage() }
        if (messages.isEmpty()) return null

        return Node.MangaPage(
            id = id,
            imageUrl = resolveImagePath(fileName, gameId, legacyId, pathProvider),
            assetId = data.assetId,
            messages = messages,
            waitMs = data.duration ?: 0,
            seenMs = data.delay ?: 0,
        )
    }

    private fun MangaMessageDto.toMangaMessage(): Node.MangaPage.MangaMessage? {
        val text = sentence?.trim()
            ?.take(MAX_MANGA_SENTENCE_LEN)
            ?.takeIf { it.isNotEmpty() }
            ?: return null
        return Node.MangaPage.MangaMessage(
            text = text,
            size = size ?: DEFAULT_MANGA_SIZE,
            x = x ?: DEFAULT_MANGA_X,
            y = y ?: DEFAULT_MANGA_Y,
            w = w ?: DEFAULT_MANGA_W,
        )
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
