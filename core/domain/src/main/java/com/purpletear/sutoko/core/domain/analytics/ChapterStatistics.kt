package com.purpletear.sutoko.core.domain.analytics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Best-effort chapter telemetry. Implementations must never block or throw to gameplay. */
interface ChapterStatistics {
    val enabled: StateFlow<Boolean>
    fun setEnabled(enabled: Boolean)
    fun entered(engine: String, gameId: String, language: String, chapter: String)
    fun active(engine: String, gameId: String, language: String, chapter: String)
    val qaEnabled: StateFlow<Boolean> get() = None.enabled
    fun preview(engine: String, gameId: String, language: String) {}
    fun playClicked(engine: String, gameId: String, language: String, isTrial: Boolean = false) {}
    fun storyFinished(engine: String, gameId: String, language: String, chapter: String, isTrial: Boolean = false) {}
    fun boundary(engine: String,gameId: String,language: String,chapter: String,nextChapter: String,available: Boolean) {}
    fun progress(engine: String,gameId: String,language: String,chapter: String,milestone: String) {}
    fun restarted(engine: String, gameId: String, language: String) {}
    fun failed(engine: String, gameId: String, language: String, chapter: String, reason: String) {}
    fun enteredTrial(engine: String, gameId: String, language: String, chapter: String) {}
    fun completedTrial(engine: String, gameId: String, language: String, chapter: String) {}
    fun completed(engine: String, gameId: String, language: String, chapter: String)

    object None : ChapterStatistics {
        override val enabled: StateFlow<Boolean> = MutableStateFlow(false)
        override fun setEnabled(enabled: Boolean) = Unit
        override fun entered(engine: String, gameId: String, language: String, chapter: String) = Unit
        override fun active(engine: String, gameId: String, language: String, chapter: String) = Unit
        override fun completed(engine: String, gameId: String, language: String, chapter: String) = Unit
    }
}
