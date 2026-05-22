package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.ChapterDao
import com.purpletear.game.data.local.dao.UserGameProgressDao
import com.purpletear.game.data.remote.ChapterApi
import com.purpletear.game.data.remote.dto.toDomain
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChapterRepositoryImpl @Inject constructor(
    private val api: ChapterApi,
    private val chapterDao: ChapterDao,
    private val userGameProgressDao: UserGameProgressDao,
) : ChapterRepository {

    override fun getChapters(storyId: String): Flow<Result<List<Chapter>>> = flow {
        val dbChapters = chapterDao.getAllForStory(storyId)
        if (dbChapters.isNotEmpty()) {
            emit(Result.success(dbChapters))
        }

        val langCode = java.util.Locale.getDefault().language
        val response = api.getChapters(storyId = storyId, langCode = langCode)

        if (response.isSuccessful) {
            val chapters = response.body()?.toDomain() ?: emptyList()
            chapterDao.insertAll(chapters)
            val freshDbChapters = chapterDao.getAllForStory(storyId)
            emit(Result.success(freshDbChapters))
        } else {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            emit(Result.failure(Exception("API call failed with code ${response.code()}: $errorBody")))
        }
    }.flowOn(Dispatchers.IO)
        .catch { e ->
            if (e is kotlinx.coroutines.CancellationException) throw e
            emit(Result.failure(e))
        }

    override fun getChapter(id: Int): Flow<Result<Chapter>> = flow {
        val dbChapter = chapterDao.getById(id.toString())
        if (dbChapter != null) {
            emit(Result.success(dbChapter))
        }

        val langCode = java.util.Locale.getDefault().language
        val response = api.getChapter(id = id, langCode = langCode)
        if (response.isSuccessful) {
            val chapter = response.body()?.toDomain()
            if (chapter != null) {
                chapterDao.insert(chapter)
                emit(Result.success(chapter))
            } else if (dbChapter == null) {
                emit(Result.failure(Exception("Chapter not found")))
            }
        } else if (dbChapter == null) {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            emit(Result.failure(Exception("API call failed with code ${response.code()}: $errorBody")))
        }
    }.flowOn(Dispatchers.IO)
        .catch { e ->
            if (e is kotlinx.coroutines.CancellationException) throw e
            emit(Result.failure(e))
        }

    override fun getCurrentChapter(gameId: String, forceReload: Boolean): Flow<Result<Chapter?>> =
        flow {
            val progress = userGameProgressDao.get(gameId)
            val code = progress?.currentChapterCode ?: DEFAULT_CHAPTER_CODE
            val chapter = chapterDao.getByStoryAndCode(gameId, code)
            emit(Result.success(chapter))
        }.flowOn(Dispatchers.IO)
            .catch { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e
                emit(Result.failure(e))
            }

    override fun observeCurrentChapter(gameId: String): Flow<Chapter?> {
        return userGameProgressDao.observe(gameId)
            .map { progress ->
                val code = progress?.currentChapterCode ?: DEFAULT_CHAPTER_CODE
                chapterDao.getByStoryAndCode(gameId, code)
            }
            .flowOn(Dispatchers.IO)
    }

    companion object {
        private const val DEFAULT_CHAPTER_CODE = "1a"
    }
}
