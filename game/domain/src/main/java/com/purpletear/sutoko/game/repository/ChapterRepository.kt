package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.Chapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChapterRepository {
    fun getChapters(storyId: String): Flow<Result<List<Chapter>>>
    fun getChapter(id: Int): Flow<Result<Chapter>>
    suspend fun refreshChapters(storyId: String)
    fun observeCachedChapters(storyId: String): StateFlow<List<Chapter>?>
    fun getCurrentChapter(gameId: String, forceReload: Boolean): Flow<Result<Chapter?>>
    fun observeCurrentChapter(gameId: String): StateFlow<Chapter?>
    suspend fun setCurrentChapter(gameId: String, chapter: Chapter)
    suspend fun restart(gameId: String)
}
