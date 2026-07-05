package com.purpletear.game.data.graph.testing

import com.google.gson.Gson
import com.purpletear.game.data.file.testing.TestAssetCacheManager
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.NodeDataDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.game.data.provider.AndroidGamePathProvider
import com.purpletear.game.data.remote.testing.dto.parseManifest
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeData
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.testing.StoryTestingLogger
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestChapterGraphLoader @Inject constructor(
    private val assetCacheManager: TestAssetCacheManager,
    private val pathProvider: AndroidGamePathProvider,
) {

    private val gson = Gson()

    /**
     * Loads a [ChapterGraph] from an extracted test package directory.
     *
     * @param extractedDirectory Path to the extracted ZIP contents.
     * @param gameId Game identifier used to locate the test asset cache.
     * @return The loaded chapter graph.
     */
    fun load(extractedDirectory: String, gameId: String): ChapterGraph {
        val extractDir = File(extractedDirectory)
        val manifestFile = File(extractDir, MANIFEST_FILE)
        require(manifestFile.exists()) { "Manifest not found in $extractedDirectory" }

        val manifest = gson.parseManifest(manifestFile.readText())
        StoryTestingLogger.d("GRPH") { "Loading graph — ${manifest.chapterId} seed ${manifest.seed}, ${manifest.nodes.size} raw nodes" }

        val assetLookup =
            buildAssetLookup(gameId, manifest.assetInventory.map { it.uniqueFileName })

        val (compactedNodes, compactedEdges) = compactIgnoreNodes(manifest.nodes, manifest.edges)
        val nodes = compactedNodes
            .mapNotNull { parseNode(it, assetLookup) }
            .associateBy { it.id }
        val edges = compactedEdges.map { parseEdge(it) }

        val startNodeId = nodes.values.filterIsInstance<Node.Start>().firstOrNull()?.id
            ?: nodes.keys.firstOrNull()
            ?: throw IllegalArgumentException("No start node found")

        StoryTestingLogger.d("GRPH") { "Graph ready — ${manifest.chapterId}: ${nodes.size} nodes, ${edges.size} edges, start=$startNodeId" }
        return ChapterGraph(
            chapterCode = manifest.chapterId,
            chapterNumber = 1,
            title = "",
            nodes = nodes,
            edges = edges,
            startNodeId = startNodeId,
        )
    }

    private fun buildAssetLookup(
        gameId: String,
        assetInventory: List<String>
    ): Map<String, String> {
        val cacheDir = assetCacheManager.getCacheDirectory(gameId)
        val cachedAssets = assetCacheManager.listCachedAssets(gameId)
        val originalAssetsDir = File(
            pathProvider.getGameDirectory(gameId, legacyId = null),
            ORIGINAL_ASSETS_DIR
        )

        val lookup = mutableMapOf<String, String>()
        assetInventory.forEach { uniqueFileName ->
            val absolutePath = resolveAbsoluteAssetPath(
                uniqueFileName = uniqueFileName,
                cachedAssets = cachedAssets,
                cacheDir = cacheDir,
                originalAssetsDir = originalAssetsDir
            ) ?: return@forEach

            lookup[uniqueFileName] = absolutePath
            lookup[File(uniqueFileName).name] = absolutePath
        }
        return lookup
    }

    private fun resolveAbsoluteAssetPath(
        uniqueFileName: String,
        cachedAssets: List<String>,
        cacheDir: File,
        originalAssetsDir: File
    ): String? {
        val fileName = File(uniqueFileName).name

        // 1. Prefer an exact cached match (preserves any subdirectory structure).
        cachedAssets.find { it == uniqueFileName }?.let {
            return File(cacheDir, it).absolutePath
        }

        // 2. Fall back to a cached file with the same file name.
        cachedAssets.find { File(it).name == fileName }?.let {
            return File(cacheDir, it).absolutePath
        }

        // 3. Delta packages omit unchanged assets; fall back to the installed story assets.
        val originalFile = File(originalAssetsDir, fileName)
        if (originalFile.exists()) {
            return originalFile.absolutePath
        }

        return null
    }

    private fun parseNode(dto: NodeDto, assetLookup: Map<String, String>): Node? {
        val data = dto.data?.let { gson.fromJson(it, NodeDataDto::class.java) } ?: return null

        return when (dto.type) {
            "start" -> Node.Start(
                id = dto.id,
                label = data.label ?: "Start"
            )

            "message" -> Node.Message(
                id = dto.id,
                text = requireNotNull(data.text) { "message node ${dto.id} missing text" },
                characterId = data.characterId ?: -1,
                waitMs = data.wait ?: 0,
                seenMs = data.seen ?: 0,
                isHesitating = data.isHesitating ?: false
            )

            "message-image" -> {
                val storagePath = data.storagePath ?: data.image
                require(storagePath != null) { "message-image node ${dto.id} missing storagePath or image" }
                Node.MessageImage(
                    id = dto.id,
                    imageUrl = resolveAssetPath(storagePath, assetLookup),
                    characterId = data.characterId ?: -1,
                    waitMs = data.wait ?: 0,
                    seenMs = data.seen ?: 0
                )
            }

            "chapter-change" -> {
                val chapterCode = data.chapterCode
                require(!chapterCode.isNullOrBlank()) { "chapter-change node ${dto.id} missing chapterCode" }
                Node.ChapterChange(id = dto.id, chapterCode = chapterCode)
            }

            "scene-node" -> Node.Scene(
                id = dto.id,
                sceneId = requireNotNull(data.sceneId) { "scene-node ${dto.id} missing sceneId" }
            )

            "condition" -> Node.Condition(
                id = dto.id,
                expression = requireNotNull(data.expression) { "condition node ${dto.id} missing expression" },
            )

            "memory", "memory-save-node" -> {
                val key = data.memory?.key ?: data.key
                val value = data.memory?.value ?: data.value
                require(!key.isNullOrBlank()) { "memory node ${dto.id} missing key" }
                require(!value.isNullOrBlank()) { "memory node ${dto.id} missing value" }
                Node.Memory(id = dto.id, key = key, value = value)
            }

            "memory-condition-node" -> {
                val key = data.memory?.key ?: data.key
                require(!key.isNullOrBlank()) { "memory-condition-node ${dto.id} missing key" }
                Node.Condition(
                    id = dto.id,
                    expression = "$key == ${data.expectedValue}",
                )
            }

            "narration" -> {
                val text = data.text
                require(!text.isNullOrBlank()) { "narration node ${dto.id} missing text" }
                Node.Info(id = dto.id, text = text)
            }

            "trophy" -> Node.Trophy(
                id = dto.id,
                trophyId = requireNotNull(data.trophyId) { "trophy node ${dto.id} missing trophyId" }
            )

            "background" -> Node.Background(
                id = dto.id,
                imageUrl = data.imageUrl?.let { resolveAssetPath(it, assetLookup) } ?: ""
            )

            "end" -> Node.End(id = dto.id)

            "sound" -> {
                val storagePath =
                    requireNotNull(data.storagePath) { "sound node ${dto.id} missing storagePath" }
                Node.Sound(
                    id = dto.id,
                    soundUrl = resolveAssetPath(storagePath, assetLookup),
                    loop = data.isLooping ?: false
                )
            }

            "message-vocal" -> {
                val storagePath =
                    requireNotNull(data.storagePath) { "message-vocal node ${dto.id} missing storagePath" }
                val characterId =
                    requireNotNull(data.characterId) { "message-vocal node ${dto.id} missing characterId" }
                Node.MessageVocal(
                    id = dto.id,
                    audioUrl = resolveAssetPath(storagePath, assetLookup),
                    characterId = characterId
                )
            }

            else -> null
        }
    }

    private fun resolveAssetPath(storagePath: String, assetLookup: Map<String, String>): String {
        if (storagePath.isBlank()) return ""
        val fileName = File(storagePath).name
        return assetLookup[fileName]
            ?: assetLookup[storagePath]
            ?: storagePath
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
                    bypassTarget?.let { edge.copy(target = it) }
                }

                else -> edge
            }
        }

        val compactedNodes = nodeDtos.filter { it.type != "ignore" }
        return compactedNodes to compactedEdges
    }

    private companion object {
        const val MANIFEST_FILE = "manifest.json"
        const val ORIGINAL_ASSETS_DIR = "assets"
    }
}
