package com.purpletear.game.presentation.game_play

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

internal class ChapterContentRequest(val chapterCode: String) {
    private val result = CompletableDeferred<Boolean>()

    val isCompleted: Boolean get() = result.isCompleted

    fun complete(chapterCode: String, ready: Boolean) {
        if (this.chapterCode == chapterCode) result.complete(ready)
    }

    suspend fun await(timeoutMillis: Long): Boolean =
        withTimeoutOrNull(timeoutMillis) { result.await() } ?: false
}
