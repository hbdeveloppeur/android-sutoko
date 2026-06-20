package com.purpletear.game.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.purpletear.sutoko.game.model.game.GameInstall

/**
 * Room entity for storing game installation records.
 * Maps to the game_installations table.
 */
@Keep
@Entity(tableName = "game_installs")
data class GameInstallEntity(
    @PrimaryKey
    val gameId: String,
    val localVersion: Int? = null
)

/**
 * Converts a Room entity to a domain model.
 */
fun GameInstallEntity.toDomain(): GameInstall =
    GameInstall(
        gameId = gameId,
        localVersion = localVersion,

        )

/**
 * Converts a domain model to a Room entity.
 */
fun GameInstall.toEntity(): GameInstallEntity =
    GameInstallEntity(
        gameId = gameId,
        localVersion = localVersion,
    )
