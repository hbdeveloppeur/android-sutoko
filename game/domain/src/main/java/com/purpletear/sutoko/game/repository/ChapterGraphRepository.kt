package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import kotlinx.coroutines.flow.Flow

interface ChapterGraphRepository {
    fun loadChapterGraph(gameId: String, chapterCode: String, language: String): Flow<Result<ChapterGraph>>
}
