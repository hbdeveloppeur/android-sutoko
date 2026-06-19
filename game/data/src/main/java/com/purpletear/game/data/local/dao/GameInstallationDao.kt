package com.purpletear.game.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.purpletear.game.data.local.entity.GameInstallEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for game installation records.
 */
@Dao
interface GameInstallationDao {
    @Query("SELECT * FROM game_installs WHERE gameId = :gameId")
    fun observeByGameId(gameId: String): Flow<GameInstallEntity?>

    @Query("SELECT * FROM game_installs")
    fun observeAll(): Flow<List<GameInstallEntity>>

    @Upsert
    suspend fun upsert(entity: GameInstallEntity)

    @Query("DELETE FROM game_installs WHERE gameId = :gameId")
    suspend fun deleteByGameId(gameId: String)

    @Query("UPDATE game_installs SET localVersion = :version WHERE gameId = :gameId")
    suspend fun markDownloaded(gameId: String, version: String)
}
