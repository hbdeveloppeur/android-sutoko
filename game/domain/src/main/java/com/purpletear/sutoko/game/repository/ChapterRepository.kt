package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.Chapter
import kotlinx.coroutines.flow.Flow

interface ChapterRepository {
    fun getChapters(storyId: String): Flow<Result<List<Chapter>>>
    fun getChapter(id: Int): Flow<Result<Chapter>>
    fun getCurrentChapter(gameId: String, forceReload: Boolean): Flow<Result<Chapter?>>
    fun observeCurrentChapter(gameId: String): Flow<Chapter?>
}
