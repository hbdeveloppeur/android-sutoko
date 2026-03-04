package com.purpletear.sutoko.game.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores user's current position in a game.
 * One row per game.
 */
@Keep
@Entity(tableName = "user_game_progress")
data class UserGameProgressEntity(
    @PrimaryKey
    val gameId: String = "",
    val currentChapterNumber: Int = 1,
    val currentAlternative: String = "",
    val heroName: String = ""
)
