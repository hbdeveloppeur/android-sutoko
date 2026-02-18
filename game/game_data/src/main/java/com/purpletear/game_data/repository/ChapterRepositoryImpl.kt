package com.purpletear.game_data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.purpletear.game_data.remote.ChapterApi
import com.purpletear.game_data.remote.dto.toDomain
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
 * Implementation of the ChapterRepository interface.
 */
class ChapterRepositoryImpl @Inject constructor(
    private val api: ChapterApi,
    private val symbols: TableOfSymbols,
    @ApplicationContext private val context: Context
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
    private val chaptersCache = mutableMapOf<Int, MutableStateFlow<List<Chapter>?>>()

    // Thread-safe and observable current chapter with gameId as key
    private val currentChapterCache = mutableMapOf<Int, MutableStateFlow<Chapter?>>()

    /**
     * Get a list of chapters for a specific story (game) ID.
     *
     * @param storyId The ID of the story (game) to retrieve chapters for.
     * @return A Flow emitting a Result containing the list of Chapters.
     */
    override fun getChapters(storyId: Int): Flow<Result<List<Chapter>>> = flow {
        try {
            // Return cached value if available
            val cachedChapters = chaptersCache[storyId]?.value
            if (cachedChapters != null) {
                emit(Result.success(cachedChapters))
            } else {
                // Fetch from API (first load)
                val startTime = System.currentTimeMillis()
                val langCode = java.util.Locale.getDefault().language
                val response = api.getChapters(storyId = storyId, langCode = langCode)
                if (response.isSuccessful) {
                    val chapters = response.body()?.toDomain() ?: emptyList()
                    // Initialize the StateFlow if it doesn't exist
                    if (!chaptersCache.containsKey(storyId)) {
                        chaptersCache[storyId] = MutableStateFlow(null)
                    }
                    chaptersCache[storyId]?.value = chapters

                    // Calculate elapsed time and add delay if needed
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime < MIN_REQUEST_DURATION) {
                        delay(MIN_REQUEST_DURATION - elapsedTime)
                    }

                    emit(Result.success(chapters))
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error - getChapters"
                    val exception =
                        Exception("API call failed with code ${response.code()}: $errorBody")
                    emit(Result.failure(exception))
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Get a specific chapter by its ID.
     *
     * @param id The ID of the chapter to retrieve.
     * @return A Flow emitting a Result containing the requested Chapter.
     */
    override fun getChapter(id: Int): Flow<Result<Chapter>> = flow {
        try {
            val langCode = java.util.Locale.getDefault().language
            val response = api.getChapter(id = id, langCode = langCode)
            if (response.isSuccessful) {
                val chapter = response.body()?.toDomain()
                if (chapter != null) {
                    emit(Result.success(chapter))
                } else {
                    emit(Result.failure(Exception("Chapter not found")))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error - getChapter"
                val exception =
                    Exception("API call failed with code ${response.code()}: $errorBody")
                emit(Result.failure(exception))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Refresh the chapters data for a specific story from the remote source.
     *
     * @param storyId The ID of the story to refresh chapters for.
     */
    override suspend fun refreshChapters(storyId: Int) {
        val langCode = java.util.Locale.getDefault().language
        val response = api.getChapters(storyId = storyId, langCode = langCode)
        if (response.isSuccessful) {
            val chapters = response.body()?.toDomain() ?: emptyList()
            // Initialize the StateFlow if it doesn't exist
            if (!chaptersCache.containsKey(storyId)) {
                chaptersCache[storyId] = MutableStateFlow(null)
            }
            chaptersCache[storyId]?.value = chapters
        }
    }

    /**
     * Observe the cached chapters data for a specific story.
     *
     * @param storyId The ID of the story to observe chapters for.
     * @return A StateFlow emitting the cached list of Chapters.
     */
    override fun observeCachedChapters(storyId: Int): StateFlow<List<Chapter>?> {
        if (!chaptersCache.containsKey(storyId)) {
            chaptersCache[storyId] = MutableStateFlow(null)
        }
        return chaptersCache[storyId]!!
    }

    /**
     * Get the current chapter for a specific game.
     *
     * @param gameId The ID of the game to get the current chapter for.
     * @return A Flow emitting a Result containing the current Chapter.
     */
    override fun getCurrentChapter(gameId: Int, forceReload: Boolean): Flow<Result<Chapter?>> =
        flow {
            try {
                context.dataStore.data.map { preferences ->
                    if (forceReload) {
                        symbols.read(context)
                    }
                    // Get the chapter code from DataStore using the game-specific key
                    val chapterCode = symbols.get(gameId, "chapterCode") ?: "1a"

                    // First try to find the chapter in the cache for this specific game
                    chaptersCache[gameId]?.value?.forEach { chapter ->
                        if (chapter.getCode() == chapterCode) {
                            // Update the current chapter cache
                            updateCurrentChapterCache(gameId, chapter)
                            return@map chapter
                        }
                    }

                    // If we couldn't find the chapter in the cache, try to parse the code
                    // to extract number and alternative
                    try {
                        val number = chapterCode.filter { it.isDigit() }.toIntOrNull() ?: 1
                        val alternative = chapterCode.filter { !it.isDigit() }.lowercase()

                        // Create a placeholder chapter with the parsed code
                        val placeholderChapter = Chapter(
                            number = number,
                            alternative = alternative,
                            title = "Chapter $number$alternative"
                        )

                        // Update the current chapter cache
                        updateCurrentChapterCache(gameId, placeholderChapter)

                        return@map placeholderChapter
                    } catch (e: Exception) {
                        // If parsing fails, return null and update cache with null
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
    override fun observeCurrentChapter(gameId: Int): StateFlow<Chapter?> {
        if (!currentChapterCache.containsKey(gameId)) {
            currentChapterCache[gameId] = MutableStateFlow(null)
        }
        return currentChapterCache[gameId]!!
    }

    /**
     * Helper method to update the current chapter cache.
     *
     * @param gameId The ID of the game to update the current chapter for.
     * @param chapter The chapter to set as current in the cache.
     */
    private fun updateCurrentChapterCache(gameId: Int, chapter: Chapter?) {
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
    override suspend fun setCurrentChapter(gameId: Int, chapter: Chapter) {
        symbols.removeFromASpecificChapterNumber(gameId, chapter.number)
        symbols.addOrSet(gameId, "chapterCode", chapter.getCode())
        symbols.save(context = context)

        // Update the current chapter cache
        updateCurrentChapterCache(gameId, chapter)
    }

    override suspend fun restart(gameId: Int) {
        symbols.removeFromASpecificChapterNumber(gameId, 1)
        symbols.addOrSet(gameId, "chapterCode", "1a")
        symbols.save(context = context)

        val firstChapter = chaptersCache[gameId]?.value?.firstOrNull()
        updateCurrentChapterCache(gameId, firstChapter)
    }
}
