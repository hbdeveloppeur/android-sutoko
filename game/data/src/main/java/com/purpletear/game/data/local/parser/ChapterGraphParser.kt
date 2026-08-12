package com.purpletear.game.data.local.parser

import com.purpletear.game.data.local.dto.ChapterMetadataDto
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.provider.GamePathProvider

/**
 * Entry point of the chapter graph parsing pipeline. Node, edge and path resolution live
 * in dedicated files of this package: [parseNode] (NodeParser.kt), [parseMangaPage],
 * [parseVisualNovel], [parseEdge] (EdgeParser.kt) and MediaPathResolver.kt.
 */
object ChapterGraphParser {

    fun parse(
        chapterCode: String,
        chapterNumber: Int = 1,
        metadata: ChapterMetadataDto,
        nodeDtos: List<NodeDto>,
        edgeDtos: List<EdgeDto>,
        gameId: String,
        legacyId: Int?,
        pathProvider: GamePathProvider,
        rightSideCharacterIds: List<Int> = emptyList()
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
            startNodeId = startNodeId,
            rightSideCharacterIds = rightSideCharacterIds
        )
    }
}
