package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.Chapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for accessing Chapter data.
 */
interface ChapterRepository {
    /**
     * Get a list of chapters for a specific story (game) ID.
     *
     * @param storyId The ID of the story (game) to retrieve chapters for.
     * @return A Flow emitting a Result containing the list of Chapters.
     */
    fun getChapters(storyId: Int): Flow<Result<List<Chapter>>>

    /**
     * Get a specific chapter by its ID.
     *
     * @param id The ID of the chapter to retrieve.
     * @return A Flow emitting a Result containing the requested Chapter.
     */
    fun getChapter(id: Int): Flow<Result<Chapter>>

    /**
     * Refresh the chapters data for a specific story from the remote source.
     *
     * @param storyId The ID of the story to refresh chapters for.
     */
    suspend fun refreshChapters(storyId: Int)

    /**
     * Observe the cached chapters data for a specific story.
     *
     * @param storyId The ID of the story to observe chapters for.
     * @return A StateFlow emitting the cached list of Chapters.
     */
    fun observeCachedChapters(storyId: Int): StateFlow<List<Chapter>?>

    /**
     * Get the current chapter for a specific game.
     *
     * @param gameId The ID of the game to get the current chapter for.
     * @return A Flow emitting the current Chapter.
     */
    fun getCurrentChapter(gameId: Int, forceReload: Boolean): Flow<Result<Chapter?>>

    /**
     * Observe the current chapter for a specific game.
     *
     * @param gameId The ID of the game to observe the current chapter for.
     * @return A StateFlow emitting the current Chapter.
     */
    fun observeCurrentChapter(gameId: Int): StateFlow<Chapter?>

    /**
     * Set the current chapter for a specific game.
     *
     * @param gameId The ID of the game to set the current chapter for.
     * @param chapter The chapter to set as current.
     */
    suspend fun setCurrentChapter(gameId: Int, chapter: Chapter)
    suspend fun restart(gameId: Int)
}
