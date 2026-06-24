package com.purpletear.game.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.purpletear.sutoko.game.model.UserGameProgress

/**
 * Stores user's current position in a game.
 * One row per game.
 */
@Keep
@Entity(tableName = "user_game_progress")
data class UserGameProgressEntity(
    @PrimaryKey
    val gameId: String = "",
    val currentChapterCode: String = "1A",
    val normalizedChapterCode: String = "1A",
    val heroName: String = ""
)

fun UserGameProgressEntity.toDomain() = UserGameProgress(
    gameId = gameId,
    currentChapterCode = currentChapterCode,
    normalizedChapterCode = normalizedChapterCode,
    heroName = heroName,
)