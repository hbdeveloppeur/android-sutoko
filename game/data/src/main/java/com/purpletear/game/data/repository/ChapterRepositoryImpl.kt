package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.ChapterDao
import com.purpletear.game.data.local.dao.UserGameProgressDao
import com.purpletear.game.data.local.entity.toDomain
import com.purpletear.game.data.local.entity.toEntity
import com.purpletear.game.data.remote.ChapterApi
import com.purpletear.game.data.remote.dto.toDomain
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

class ChapterRepositoryImpl @Inject constructor(
    private val api: ChapterApi,
    private val chapterDao: ChapterDao,
    private val userGameProgressDao: UserGameProgressDao,
) : ChapterRepository {

    override fun getChapters(storyId: String): Flow<Result<List<Chapter>>> = flow {
        val dbChapters = chapterDao.getAllForStory(storyId).map { it.toDomain() }
        if (dbChapters.isNotEmpty()) {
            emit(Result.success(dbChapters))
        }

        try {
            val response =
                api.getChapters(storyId = storyId, langCode = Locale.getDefault().language)
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
        return userGameProgressDao.observe(gameId)
            .flatMapLatest { progress ->
                val code = progress?.currentChapterCode ?: DEFAULT_CHAPTER_CODE
                chapterDao.observeByStoryAndCode(gameId, code)
            }
            .map { it?.toDomain() }
            .flowOn(Dispatchers.IO)
    }

    companion object {
        private const val DEFAULT_CHAPTER_CODE = "1a"
    }
}
