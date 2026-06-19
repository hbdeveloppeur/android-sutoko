package com.purpletear.sutoko.game.model

import androidx.annotation.Keep

/**
 * Stores user's current position in a game.
 * One row per game.
 */
@Keep
data class UserGameProgress(
    val gameId: String = "",
    val currentChapterCode: String = "1a",
    val normalizedChapterCode: String = "1a",
    val heroName: String = ""
)
