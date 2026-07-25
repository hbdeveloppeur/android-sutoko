package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.Chapter
import kotlinx.coroutines.flow.Flow

interface ChapterRepository {
    fun getChapters(storyId: String): Flow<Result<List<Chapter>>>

    /**
     * Observes the chapters stored locally for [storyId]. Emits an empty list
     * until a load (see [getChapters]) populates the local store.
     */
    fun observeChapters(storyId: String): Flow<List<Chapter>>

    /**
     * Observes the ids of stories having at least one locally cached chapter whose
     * release date is in the future. Empty until [getChapters] populates the store.
     */
    fun observeStoryIdsWithUpcomingChapters(): Flow<Set<String>>
    fun getChapter(id: Int): Flow<Result<Chapter>>
    fun getCurrentChapter(gameId: String, forceReload: Boolean): Flow<Result<Chapter?>>
    fun observeCurrentChapter(gameId: String): Flow<Chapter?>
}
