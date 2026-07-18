package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeChapterRepository : ChapterRepository {
    private val chapters = mutableMapOf<String, MutableStateFlow<Result<List<Chapter>>>>()
    private val currentChapters = mutableMapOf<String, MutableStateFlow<Chapter?>>()

    var getChaptersCalls = 0
        private set

    /** When non-null, getChapters suspends after the first emission until this gate completes. */
    var getChaptersGate: CompletableDeferred<Unit>? = null

    fun setChapters(storyId: String, result: Result<List<Chapter>>) {
        chapters.getOrPut(storyId) { MutableStateFlow(result) }.value = result
    }

    fun setCurrentChapter(gameId: String, chapter: Chapter?) {
        currentChapters.getOrPut(gameId) { MutableStateFlow(chapter) }.value = chapter
    }

    override fun getChapters(storyId: String): Flow<Result<List<Chapter>>> {
        val source = chapters.getOrPut(storyId) { MutableStateFlow(Result.success(emptyList())) }
        val gate = getChaptersGate
        // Mirrors the real implementation contract: a finite flow.
        return flow {
            getChaptersCalls++
            emit(source.value)
            gate?.await()
        }
    }

    override fun getChapter(id: Int): Flow<Result<Chapter>> {
        return flowOf(Result.failure(UnsupportedOperationException()))
    }

    override fun getCurrentChapter(gameId: String, forceReload: Boolean): Flow<Result<Chapter?>> {
        return currentChapters.getOrPut(gameId) { MutableStateFlow(null) }
            .asStateFlow()
            .let { source ->
                flow {
                    source.collect { emit(Result.success(it)) }
                }
            }
    }

    override fun observeCurrentChapter(gameId: String): Flow<Chapter?> {
        return currentChapters.getOrPut(gameId) { MutableStateFlow(null) }.asStateFlow()
    }
}
