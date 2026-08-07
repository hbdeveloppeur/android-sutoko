package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.ChapterDao
import com.purpletear.game.data.local.dao.GameDao
import com.purpletear.game.data.local.dao.UserGameProgressDao
import com.purpletear.game.data.local.entity.toDomain
import com.purpletear.game.data.local.entity.toEntity
import com.purpletear.game.data.remote.ChapterApi
import com.purpletear.game.data.remote.dto.toDomain
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.FriendzonedLegacyIds
import com.purpletear.sutoko.game.repository.ChapterRepository
import com.purpletear.sutoko.game.repository.FriendzonedProgressRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

class ChapterRepositoryImpl @Inject constructor(
    private val api: ChapterApi,
    private val chapterDao: ChapterDao,
    private val userGameProgressDao: UserGameProgressDao,
    private val gameDao: GameDao,
    private val friendzonedProgressRepository: FriendzonedProgressRepository,
    private val userRepository: UserRepository,
) : ChapterRepository {

    /** Optional bearer token: admins also receive unreleased chapters. */
    private suspend fun bearerToken(): String? =
        userRepository.observeUser().firstOrNull()?.token?.let { "Bearer $it" }

    override fun getChapters(storyId: String): Flow<Result<List<Chapter>>> = flow {
        val dbChapters = chapterDao.getAllForStory(storyId).map { it.toDomain() }
        if (dbChapters.isNotEmpty()) {
            emit(Result.success(dbChapters))
        }

        try {
            val response = api.getChapters(
                storyId = storyId,
                langCode = Locale.getDefault().language,
                authorization = bearerToken(),
            )
            if (response.isSuccessful) {
                val chapters = response.body()?.toDomain() ?: emptyList()
                chapterDao.insertAll(chapters.map { it.toEntity() })
                val freshDbChapters = chapterDao.getAllForStory(storyId).map { it.toDomain() }
                emit(Result.success(freshDbChapters))
            } else if (dbChapters.isEmpty()) {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                emit(Result.failure(Exception("API call failed with code ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (dbChapters.isEmpty()) {
                emit(Result.failure(e))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun observeChapters(storyId: String): Flow<List<Chapter>> =
        chapterDao.observeAllForStory(storyId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override fun observeStoryIdsWithUpcomingChapters(): Flow<Set<String>> = flow {
        // "now" is fixed at collection start; releaseDate is epoch seconds (server format).
        emitAll(chapterDao.observeStoryIdsWithUpcomingChapters(System.currentTimeMillis() / 1000))
    }.map { it.toSet() }.flowOn(Dispatchers.IO)

    override fun getChapter(id: Int): Flow<Result<Chapter>> = flow {
        val dbChapter = chapterDao.getById(id.toString())?.toDomain()
        if (dbChapter != null) {
            emit(Result.success(dbChapter))
        }

        try {
            val response = api.getChapter(id = id, langCode = Locale.getDefault().language)
            if (response.isSuccessful) {
                val chapter = response.body()?.toDomain()
                if (chapter != null) {
                    chapterDao.insert(chapter.toEntity())
                    emit(Result.success(chapter))
                } else if (dbChapter == null) {
                    emit(Result.failure(Exception("Chapter not found")))
                }
            } else if (dbChapter == null) {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                emit(Result.failure(Exception("API call failed with code ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (dbChapter == null) {
                emit(Result.failure(e))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getCurrentChapter(gameId: String, forceReload: Boolean): Flow<Result<Chapter?>> =
        flow {
            val progress = userGameProgressDao.get(gameId)
            val code = progress?.currentChapterCode ?: DEFAULT_CHAPTER_CODE
            val chapter = chapterDao.getByStoryAndCode(gameId, code)?.toDomain()
            emit(Result.success(chapter))
        }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCurrentChapter(gameId: String): Flow<Chapter?> {
        return gameDao.observeGame(gameId)
            .flatMapLatest { game ->
                val legacyId = game?.legacyId
                if (FriendzonedLegacyIds.isFriendzoned(legacyId)) {
                    observeFriendzonedCurrentChapter(gameId, legacyId!!)
                } else {
                    observeProgressCurrentChapter(gameId)
                }
            }
            .flowOn(Dispatchers.IO)
    }

    /** Standard engine: the current chapter comes from the Room user-progress row. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeProgressCurrentChapter(gameId: String): Flow<Chapter?> {
        return userGameProgressDao.observe(gameId)
            .flatMapLatest { progress ->
                val code = progress?.currentChapterCode ?: DEFAULT_CHAPTER_CODE
                chapterDao.observeByStoryAndCode(gameId, code)
            }
            .map { it?.toDomain() }
    }

    /**
     * Friendzoned games persist their progress in TableOfSymbols, never in the
     * Room user-progress row. The code is read once per collection (callers
     * re-collect on ON_RESUME); only the chapter data itself is observed.
     * Codes live in two namespaces (symbols "7a" vs catalog "7A"): exact
     * match first, then same chapter number, then the story's first chapter.
     */
    private fun observeFriendzonedCurrentChapter(gameId: String, legacyId: Int): Flow<Chapter?> =
        flow {
            val code = friendzonedProgressRepository.getChapterCode(legacyId)
            val chapterNumber = code.dropLast(1).toIntOrNull()
            emitAll(
                chapterDao.observeAllForStory(gameId).map { entities ->
                    val chapters = entities.map { it.toDomain() }
                    chapters.firstOrNull { it.code.equals(code, ignoreCase = true) }
                        ?: chapters.firstOrNull { it.number == chapterNumber }
                        ?: chapters.firstOrNull()
                }
            )
        }

    companion object {
        private const val DEFAULT_CHAPTER_CODE = "1A"
    }
}
