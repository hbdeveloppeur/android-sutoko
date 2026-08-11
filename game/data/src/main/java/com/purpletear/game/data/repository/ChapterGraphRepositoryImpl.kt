package com.purpletear.game.data.repository

import android.os.Trace
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log
import com.purpletear.game.data.local.dao.ChapterDao
import com.purpletear.game.data.local.dao.GameInstallationDao
import com.purpletear.game.data.local.dto.ChapterMetadataDto
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.LayoutDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.game.data.local.parser.ChapterGraphParser
import com.purpletear.game.data.provider.AndroidGamePathProvider
import com.purpletear.game.data.provider.ChapterLanguageResolver
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.repository.ChapterGraphRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class ChapterGraphRepositoryImpl @Inject constructor(
    private val pathProvider: AndroidGamePathProvider,
    private val chapterDao: ChapterDao,
    private val gameRepository: GameRepository,
    private val installDao: GameInstallationDao,
) : ChapterGraphRepository {

    private val gson = Gson()

    override fun loadChapterGraph(
        gameId: String,
        chapterCode: String,
        language: String
    ): Flow<Result<ChapterGraph>> = flow {
        Trace.beginSection("ChapterGraphRepositoryImpl.loadChapterGraph")
        try {
            val legacyId = gameRepository.observeGame(gameId).firstOrNull()?.legacyId
            val gameDir = pathProvider.getGameDirectory(gameId, legacyId)
            val chaptersRoot = File(gameDir, "chapters")
            val availableLanguages = chaptersRoot.listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?: emptyList()

            val resolvedLanguage = ChapterLanguageResolver.resolve(language, availableLanguages)
            if (resolvedLanguage == null) {
                if (gameDir.exists()) {
                    // Broken install on disk (older builds could mark a corrupt archive as
                    // installed): clear it so the UI falls back to Download instead of
                    // failing on every play.
                    runCatching { gameDir.deleteRecursively() }
                    runCatching { installDao.deleteByGameId(gameId) }
                }
                emit(Result.failure(IllegalArgumentException("No chapter language '$language' found in: ${chaptersRoot.absolutePath} (available: $availableLanguages)")))
                return@flow
            }

            val chapterDir = File(chaptersRoot, "$resolvedLanguage/${chapterCode.lowercase()}")

            if (!chapterDir.exists()) {
                emit(Result.failure(IllegalArgumentException("Chapter directory not found: ${chapterDir.absolutePath}")))
                return@flow
            }

            val metadataFile = File(chapterDir, "metadata.json")
            val nodesFile = File(chapterDir, "nodes.json")
            val edgesFile = File(chapterDir, "edges.json")
            val layoutFile = File(chapterDir, "layout.json")

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

            val layout: LayoutDto? = if (layoutFile.exists()) {
                gson.fromJson(layoutFile.readText(), LayoutDto::class.java)
            } else {
                null
            }
            Log.d("ChapterGraph", "chapter=$chapterCode layout.json=${if (layoutFile.exists()) "found" else "missing"} rightSideIds=${layout?.sides?.right.orEmpty()}")

            val chapterNumber = chapterDao.getByStoryAndCode(gameId, chapterCode)?.number ?: 1

            val graph = ChapterGraphParser.parse(
                chapterCode = chapterCode,
                chapterNumber = chapterNumber,
                metadata = metadata,
                nodeDtos = nodeDtos,
                edgeDtos = edgeDtos,
                gameId = gameId,
                legacyId = legacyId,
                pathProvider = pathProvider,
                rightSideCharacterIds = layout?.sides?.right.orEmpty()
            )
            emit(Result.success(graph))

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(
                Result.failure(
                    IllegalStateException("Failed to load chapter $chapterCode: ${e.message}", e)
                )
            )
        } finally {
            Trace.endSection()
        }
    }.flowOn(Dispatchers.IO)
}
