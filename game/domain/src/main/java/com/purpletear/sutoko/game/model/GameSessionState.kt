package com.purpletear.sutoko.game.model

import androidx.annotation.Keep

/**
 * Represents all possible states of a game session.
 * UI reacts to these states to show appropriate screens.
 */
@Keep
sealed class GameSessionState {
    object Loading : GameSessionState()

    data class Error(
        val type: ErrorType,
        val message: String
    ) : GameSessionState()

    data class Ready(
        val gameId: String,
        val chapter: Chapter,
        val heroName: String,
        val totalChapters: Int
    ) : GameSessionState()
}

@Keep
enum class ErrorType {
    GAME_NOT_FOUND,
    GAME_NOT_INSTALLED,
    GAME_UPDATE_REQUIRED,
    CHAPTER_UNAVAILABLE,
    CHAPTER_NOT_FOUND,
    NO_CHAPTERS_FOUND,
    UNKNOWN
}
