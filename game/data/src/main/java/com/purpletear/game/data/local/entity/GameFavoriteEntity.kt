package com.purpletear.game.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing favorite game records.
 * Maps to the game_favorites table.
 */
@Keep
@Entity(tableName = "game_favorites")
data class GameFavoriteEntity(
    @PrimaryKey
    val gameId: String,
    val addedAt: Long,
)
