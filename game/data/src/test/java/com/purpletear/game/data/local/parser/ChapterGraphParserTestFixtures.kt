package com.purpletear.game.data.local.parser

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.purpletear.game.data.local.dto.ChapterMetadataDto
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.provider.GamePathProvider

private val fixtureGson = Gson()

internal fun node(
    id: String,
    type: String,
    text: String? = null,
    characterId: Int? = null,
    expression: String? = null,
    data: JsonElement? = null
): NodeDto {
    val dataObject = data as? JsonObject ?: JsonObject().apply {
        text?.let { addProperty("text", it) }
        characterId?.let { addProperty("characterId", it) }
        expression?.let { addProperty("expression", it) }
    }
    return NodeDto(id = id, type = type, data = dataObject)
}

internal fun edge(
    source: String,
    target: String,
    type: String? = null,
    edgeType: String? = null
): EdgeDto {
    val json = buildString {
        append("{\"source\":\"$source\",\"target\":\"$target\"")
        if (type != null) append(",\"type\":\"$type\"")
        if (edgeType != null) append(",\"data\":{\"edgeType\":\"$edgeType\"}")
        append("}")
    }
    return fixtureGson.fromJson(json, EdgeDto::class.java)
}

internal fun parseGraph(
    nodes: List<NodeDto>,
    edges: List<EdgeDto>,
    chapterCode: String = "2a",
    gameId: String = "game1",
    legacyId: Int? = null,
    pathProvider: GamePathProvider = FakeGamePathProvider()
) = ChapterGraphParser.parse(
    chapterCode = chapterCode,
    metadata = ChapterMetadataDto(title = "Chapter ${chapterCode.uppercase()}"),
    nodeDtos = nodes,
    edgeDtos = edges,
    gameId = gameId,
    legacyId = legacyId,
    pathProvider = pathProvider
)

internal class FakeGamePathProvider(
    private val storiesPath: String = "/tmp/games"
) : GamePathProvider {
    override fun getStoriesDirectoryPath(): String = storiesPath
    override fun getStoryDirectoryPath(storyId: String, legacyId: Int?): String =
        "$storiesPath/${legacyId ?: storyId}"
}
