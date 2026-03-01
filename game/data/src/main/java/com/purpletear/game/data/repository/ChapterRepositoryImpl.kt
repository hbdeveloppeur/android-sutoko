package com.purpletear.game.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.purpletear.game.data.local.dao.ChapterDao
import com.purpletear.game.data.remote.ChapterApi
import com.purpletear.game.data.remote.dto.toDomain
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import purpletear.fr.purpleteartools.TableOfSymbols
import javax.inject.Inject

class ChapterRepositoryImpl @Inject constructor(
    private val api: ChapterApi,
    private val chapterDao: ChapterDao,
    private val symbols: TableOfSymbols,
    @field:ApplicationContext private val context: Context
) : ChapterRepository {

    companion object {
        private const val MIN_REQUEST_DURATION = 560L
        private const val DATASTORE_NAME = "chapter_preferences"
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)
    private val chaptersCache = mutableMapOf<String, MutableStateFlow<List<Chapter>?>>()
    private val currentChapterCache = mutableMapOf<String, MutableStateFlow<Chapter?>>()

    override fun getChapters(storyId: String): Flow<Result<List<Chapter>>> = flow {
        try {
            val dbChapters = chapterDao.getAllForStory(storyId)
            if (dbChapters.isNotEmpty()) {
                emit(Result.success(dbChapters))
                updateChaptersCache(storyId, dbChapters)
            }

            val startTime = System.currentTimeMillis()
            val langCode = java.util.Locale.getDefault().language
            val response = api.getChapters(storyId = storyId, langCode = langCode)

            if (response.isSuccessful) {
                val chapters = response.body()?.toDomain() ?: emptyList()
                chapterDao.insertAll(chapters)
                updateChaptersCache(storyId, chapters)

                val elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime < MIN_REQUEST_DURATION) {
                    delay(MIN_REQUEST_DURATION - elapsedTime)
                }

                val freshDbChapters = chapterDao.getAllForStory(storyId)
                emit(Result.success(freshDbChapters))
            } else {
                if (dbChapters.isEmpty()) {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error - getChapters"
                    emit(Result.failure(Exception("API call failed with code ${response.code()}: $errorBody")))
                }
            }
        } catch (e: Exception) {
            val cachedChapters = chaptersCache[storyId]?.value
            val dbChapters = chapterDao.getAllForStory(storyId)
            
            when {
                cachedChapters != null -> emit(Result.success(cachedChapters))
                dbChapters.isNotEmpty() -> emit(Result.success(dbChapters))
                else -> emit(Result.failure(e))
            }
        }
    }

    override fun getChapter(id: Int): Flow<Result<Chapter>> = flow {
        try {
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
                val errorBody = response.errorBody()?.string() ?: "Unknown error - getChapter"
                emit(Result.failure(Exception("API call failed with code ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun refreshChapters(storyId: String) {
        val langCode = java.util.Locale.getDefault().language
        val response = api.getChapters(storyId = storyId, langCode = langCode)
        if (response.isSuccessful) {
            val chapters = response.body()?.toDomain() ?: emptyList()
            chapterDao.insertAll(chapters)
            updateChaptersCache(storyId, chapters)
        }
    }

    override fun observeCachedChapters(storyId: String): StateFlow<List<Chapter>?> {
        if (!chaptersCache.containsKey(storyId)) {
            chaptersCache[storyId] = MutableStateFlow(null)
        }
        return chaptersCache[storyId]!!
    }

    override fun getCurrentChapter(gameId: String, forceReload: Boolean): Flow<Result<Chapter?>> =
        flow {
            try {
                context.dataStore.data.map { preferences ->
                    if (forceReload) {
                        symbols.read(context)
                    }
                    val gameIdHash = gameId.hashCode()
                    val chapterCode = symbols.get(gameIdHash, "chapterCode") ?: "1a"

                    val dbChapter = chapterDao.getByStoryAndCode(gameId, chapterCode)
                    if (dbChapter != null) {
                        updateCurrentChapterCache(gameId, dbChapter)
                        return@map dbChapter
                    }

                    chaptersCache[gameId]?.value?.forEach { chapter ->
                        if (chapter.code == chapterCode) {
                            updateCurrentChapterCache(gameId, chapter)
                            return@map chapter
                        }
                    }

                    try {
                        val number = chapterCode.filter { it.isDigit() }.toIntOrNull() ?: 1
                        val alternative = chapterCode.filter { !it.isDigit() }.lowercase()

                        val placeholderChapter = Chapter(
                            number = number,
                            alternative = alternative,
                            title = "Chapter $number$alternative",
                            code = chapterCode
                        )

                        updateCurrentChapterCache(gameId, placeholderChapter)
                        placeholderChapter
                    } catch (e: Exception) {
                        updateCurrentChapterCache(gameId, null)
                        null
                    }
                }.collect { chapter ->
                    emit(Result.success(chapter))
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }

    override fun observeCurrentChapter(gameId: String): StateFlow<Chapter?> {
        if (!currentChapterCache.containsKey(gameId)) {
            currentChapterCache[gameId] = MutableStateFlow(null)
        }
        return currentChapterCache[gameId]!!
    }

    override suspend fun setCurrentChapter(gameId: String, chapter: Chapter) {
        val gameIdHash = gameId.hashCode()
        symbols.removeFromASpecificChapterNumber(gameIdHash, chapter.number)
        symbols.addOrSet(gameIdHash, "chapterCode", chapter.code)
        symbols.save(context = context)

        val existingChapter = chapterDao.getById(chapter.id)
        if (existingChapter == null) {
            chapterDao.insert(chapter)
        }

        updateCurrentChapterCache(gameId, chapter)
    }

    override suspend fun restart(gameId: String) {
        val gameIdHash = gameId.hashCode()
        symbols.removeFromASpecificChapterNumber(gameIdHash, 1)
        symbols.addOrSet(gameIdHash, "chapterCode", "1a")
        symbols.save(context = context)

        val firstChapter = chapterDao.getAllForStory(gameId).firstOrNull()
            ?: chaptersCache[gameId]?.value?.firstOrNull()
        updateCurrentChapterCache(gameId, firstChapter)
    }

    private fun updateChaptersCache(storyId: String, chapters: List<Chapter>) {
        if (!chaptersCache.containsKey(storyId)) {
            chaptersCache[storyId] = MutableStateFlow(null)
        }
        chaptersCache[storyId]?.value = chapters
    }

    private fun updateCurrentChapterCache(gameId: String, chapter: Chapter?) {
        if (!currentChapterCache.containsKey(gameId)) {
            currentChapterCache[gameId] = MutableStateFlow(null)
        }
        currentChapterCache[gameId]?.value = chapter
    }
}
