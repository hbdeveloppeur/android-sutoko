package com.purpletear.game.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.purpletear.sutoko.game.model.GameInstallation

/**
 * Room entity for storing game installation records.
 * Maps to the game_installations table.
 */
@Keep
@Entity(tableName = "game_installations")
data class GameInstallationEntity(
    @PrimaryKey
    val gameId: String = "",
    val installedVersion: String = "",
    val installedAt: Long = System.currentTimeMillis()
)

/**
 * Converts a Room entity to a domain model.
 */
fun GameInstallationEntity.toDomain(): GameInstallation =
    GameInstallation(
        gameId = gameId,
        installedVersion = installedVersion,
        installedAt = installedAt
    )

/**
 * Converts a domain model to a Room entity.
 */
fun GameInstallation.toEntity(): GameInstallationEntity =
    GameInstallationEntity(
        gameId = gameId,
        installedVersion = installedVersion,
        installedAt = installedAt
    )
