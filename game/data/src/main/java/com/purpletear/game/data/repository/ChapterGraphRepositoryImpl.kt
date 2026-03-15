package com.purpletear.game.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.purpletear.game.data.local.dto.ChapterMetadataDto
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.game.data.local.parser.ChapterGraphParser
import com.purpletear.game.data.provider.GamePathProvider
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.repository.ChapterGraphRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

class ChapterGraphRepositoryImpl @Inject constructor(
    private val pathProvider: GamePathProvider
) : ChapterGraphRepository {

    private val gson = Gson()

    override fun loadChapterGraph(
        gameId: String,
        chapterCode: String,
        language: String
    ): Flow<Result<ChapterGraph>> = flow {
        try {
            val baseDir = File(pathProvider.getGamesDirectory(), gameId)
            val chapterDir = File(baseDir, "chapters/$language/${chapterCode.lowercase()}")

            if (!chapterDir.exists()) {
                emit(Result.failure(IllegalArgumentException("Chapter directory not found: ${chapterDir.absolutePath}")))
                return@flow
            }

            val metadataFile = File(chapterDir, "metadata.json")
            val nodesFile = File(chapterDir, "nodes.json")
            val edgesFile = File(chapterDir, "edges.json")

            if (!nodesFile.exists()) {
                emit(Result.failure(IllegalArgumentException("Nodes file not found: ${nodesFile.absolutePath}")))
                return@flow
            }

            val metadata = if (metadataFile.exists()) {
                gson.fromJson(metadataFile.readText(), ChapterMetadataDto::class.java)
            } else {
                ChapterMetadataDto(title = "Chapter $chapterCode")
            }

            val nodeListType = object : TypeToken<List<NodeDto>>() {}.type
            val nodeDtos: List<NodeDto> = gson.fromJson(nodesFile.readText(), nodeListType)
            
            val edgeDtos = if (edgesFile.exists()) {
                val edgeListType = object : TypeToken<List<EdgeDto>>() {}.type
                gson.fromJson<List<EdgeDto>>(edgesFile.readText(), edgeListType)
            } else {
                emptyList()
            }

            val graph = ChapterGraphParser.parse(chapterCode, metadata, nodeDtos, edgeDtos)
            emit(Result.success(graph))

        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            emit(Result.failure(e))
        }
    }
}
