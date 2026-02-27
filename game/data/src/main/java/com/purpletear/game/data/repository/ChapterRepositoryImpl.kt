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

/**
 * Implementation of the ChapterRepository interface with offline-first strategy.
 */
class ChapterRepositoryImpl @Inject constructor(
    private val api: ChapterApi,
    private val chapterDao: ChapterDao,
    private val symbols: TableOfSymbols,
    @field:ApplicationContext private val context: Context
) : ChapterRepository {

    companion object {
        // Minimum request duration in milliseconds
        private const val MIN_REQUEST_DURATION = 560L

        // DataStore name
        private const val DATASTORE_NAME = "chapter_preferences"
    }

    // Create DataStore instance
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

    // Thread-safe and observable cache with storyId as key
    private val chaptersCache = mutableMapOf<String, MutableStateFlow<List<Chapter>?>>()

    // Thread-safe and observable current chapter with gameId as key
    private val currentChapterCache = mutableMapOf<String, MutableStateFlow<Chapter?>>()

    /**
     * Get a list of chapters for a specific story (game) ID.
     * Offline-first: emits from DB first, then fetches from API and updates DB.
     *
     * @param storyId The ID of the story (game) to retrieve chapters for.
     * @return A Flow emitting a Result containing the list of Chapters.
     */
    override fun getChapters(storyId: String): Flow<Result<List<Chapter>>> = flow {
        try {
            // Step 1: Emit from database first (offline-first)
            val dbChapters = chapterDao.getAllForStory(storyId)
            if (dbChapters.isNotEmpty()) {
                emit(Result.success(dbChapters))
                // Update in-memory cache
                updateChaptersCache(storyId, dbChapters)
            }

            // Step 2: Fetch from API
            val startTime = System.currentTimeMillis()
            val langCode = java.util.Locale.getDefault().language
            val response = api.getChapters(storyId = storyId, langCode = langCode)

            if (response.isSuccessful) {
                val chapters = response.body()?.toDomain() ?: emptyList()

                // Step 3: Save to database
                chapterDao.insertAll(chapters)

                // Step 4: Update in-memory cache
                updateChaptersCache(storyId, chapters)

                // Step 5: Calculate elapsed time and add delay if needed
                val elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime < MIN_REQUEST_DURATION) {
                    delay(MIN_REQUEST_DURATION - elapsedTime)
                }

                // Step 6: Emit fresh data from database
                val freshDbChapters = chapterDao.getAllForStory(storyId)
                emit(Result.success(freshDbChapters))
            } else {
                // If API fails but we have DB data, don't emit error (offline support)
                if (dbChapters.isEmpty()) {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error - getChapters"
                    val exception = Exception("API call failed with code ${response.code()}: $errorBody")
                    emit(Result.failure(exception))
                }
            }
        } catch (e: Exception) {
            // If exception occurs, check if we have cached data
            val cachedChapters = chaptersCache[storyId]?.value
            val dbChapters = chapterDao.getAllForStory(storyId)
            
            if (cachedChapters != null) {
                emit(Result.success(cachedChapters))
            } else if (dbChapters.isNotEmpty()) {
                emit(Result.success(dbChapters))
            } else {
                emit(Result.failure(e))
            }
        }
    }

    /**
     * Get a specific chapter by its ID.
     * Checks database first, falls back to API.
     *
     * @param id The ID of the chapter to retrieve.
     * @return A Flow emitting a Result containing the requested Chapter.
     */
    override fun getChapter(id: Int): Flow<Result<Chapter>> = flow {
        try {
            // First try to get from database
            val dbChapter = chapterDao.getById(id.toString())
            if (dbChapter != null) {
                emit(Result.success(dbChapter))
            }

            // Fetch from API for fresh data
            val langCode = java.util.Locale.getDefault().language
            val response = api.getChapter(id = id, langCode = langCode)
            if (response.isSuccessful) {
                val chapter = response.body()?.toDomain()
                if (chapter != null) {
                    // Save to database
                    chapterDao.insert(chapter)
                    emit(Result.success(chapter))
                } else if (dbChapter == null) {
                    emit(Result.failure(Exception("Chapter not found")))
                }
            } else if (dbChapter == null) {
                val errorBody = response.errorBody()?.string() ?: "Unknown error - getChapter"
                val exception = Exception("API call failed with code ${response.code()}: $errorBody")
                emit(Result.failure(exception))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Refresh the chapters data for a specific story from the remote source.
     * Forces API fetch and updates database.
     *
     * @param storyId The ID of the story to refresh chapters for.
     */
    override suspend fun refreshChapters(storyId: String) {
        val langCode = java.util.Locale.getDefault().language
        val response = api.getChapters(storyId = storyId, langCode = langCode)
        if (response.isSuccessful) {
            val chapters = response.body()?.toDomain() ?: emptyList()
            
            // Save to database
            chapterDao.insertAll(chapters)
            
            // Update in-memory cache
            updateChaptersCache(storyId, chapters)
        }
    }

    /**
     * Observe the cached chapters data for a specific story.
     *
     * @param storyId The ID of the story to observe chapters for.
     * @return A StateFlow emitting the cached list of Chapters.
     */
    override fun observeCachedChapters(storyId: String): StateFlow<List<Chapter>?> {
        if (!chaptersCache.containsKey(storyId)) {
            chaptersCache[storyId] = MutableStateFlow(null)
        }
        return chaptersCache[storyId]!!
    }

    /**
     * Get the current chapter for a specific game.
     * Checks database first, then in-memory cache.
     *
     * @param gameId The ID of the game to get the current chapter for.
     * @return A Flow emitting a Result containing the current Chapter.
     */
    override fun getCurrentChapter(gameId: String, forceReload: Boolean): Flow<Result<Chapter?>> =
        flow {
            try {
                context.dataStore.data.map { preferences ->
                    if (forceReload) {
                        symbols.read(context)
                    }
                    // Get the chapter code from DataStore using the game-specific key
                    val gameIdHash = gameId.hashCode()
                    val chapterCode = symbols.get(gameIdHash, "chapterCode") ?: "1a"

                    // First try to find the chapter in the database
                    val dbChapter = chapterDao.getByStoryAndCode(gameId, chapterCode)
                    if (dbChapter != null) {
                        updateCurrentChapterCache(gameId, dbChapter)
                        return@map dbChapter
                    }

                    // Then try in-memory cache
                    chaptersCache[gameId]?.value?.forEach { chapter ->
                        if (chapter.code == chapterCode) {
                            updateCurrentChapterCache(gameId, chapter)
                            return@map chapter
                        }
                    }

                    // If we couldn't find the chapter, try to parse the code
                    try {
                        val number = chapterCode.filter { it.isDigit() }.toIntOrNull() ?: 1
                        val alternative = chapterCode.filter { !it.isDigit() }.lowercase()

                        // Create a placeholder chapter with the parsed code
                        val placeholderChapter = Chapter(
                            number = number,
                            alternative = alternative,
                            title = "Chapter $number$alternative",
                            code = chapterCode
                        )

                        updateCurrentChapterCache(gameId, placeholderChapter)
                        return@map placeholderChapter
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

    /**
     * Observe the current chapter for a specific game.
     *
     * @param gameId The ID of the game to observe the current chapter for.
     * @return A StateFlow emitting the current Chapter.
     */
    override fun observeCurrentChapter(gameId: String): StateFlow<Chapter?> {
        if (!currentChapterCache.containsKey(gameId)) {
            currentChapterCache[gameId] = MutableStateFlow(null)
        }
        return currentChapterCache[gameId]!!
    }

    /**
     * Helper method to update the chapters cache.
     *
     * @param storyId The ID of the story to update the cache for.
     * @param chapters The chapters to set in the cache.
     */
    private fun updateChaptersCache(storyId: String, chapters: List<Chapter>) {
        if (!chaptersCache.containsKey(storyId)) {
            chaptersCache[storyId] = MutableStateFlow(null)
        }
        chaptersCache[storyId]?.value = chapters
    }

    /**
     * Helper method to update the current chapter cache.
     *
     * @param gameId The ID of the game to update the current chapter for.
     * @param chapter The chapter to set as current in the cache.
     */
    private fun updateCurrentChapterCache(gameId: String, chapter: Chapter?) {
        if (!currentChapterCache.containsKey(gameId)) {
            currentChapterCache[gameId] = MutableStateFlow(null)
        }
        currentChapterCache[gameId]?.value = chapter
    }

    /**
     * Set the current chapter for a specific game.
     *
     * @param gameId The ID of the game to set the current chapter for.
     * @param chapter The chapter to set as current.
     */
    override suspend fun setCurrentChapter(gameId: String, chapter: Chapter) {
        val gameIdHash = gameId.hashCode()
        symbols.removeFromASpecificChapterNumber(gameIdHash, chapter.number)
        symbols.addOrSet(gameIdHash, "chapterCode", chapter.code)
        symbols.save(context = context)

        // Save chapter to database if not exists
        val existingChapter = chapterDao.getById(chapter.id)
        if (existingChapter == null) {
            chapterDao.insert(chapter)
        }

        // Update the current chapter cache
        updateCurrentChapterCache(gameId, chapter)
    }

    override suspend fun restart(gameId: String) {
        val gameIdHash = gameId.hashCode()
        symbols.removeFromASpecificChapterNumber(gameIdHash, 1)
        symbols.addOrSet(gameIdHash, "chapterCode", "1a")
        symbols.save(context = context)

        // Try to get first chapter from database, then from cache
        val firstChapter = chapterDao.getAllForStory(gameId).firstOrNull()
            ?: chaptersCache[gameId]?.value?.firstOrNull()
        updateCurrentChapterCache(gameId, firstChapter)
    }
}
