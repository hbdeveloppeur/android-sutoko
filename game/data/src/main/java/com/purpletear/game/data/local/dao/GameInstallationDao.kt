package com.purpletear.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.purpletear.game.data.local.entity.GameInstallationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for game installation records.
 */
@Dao
interface GameInstallationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GameInstallationEntity)

    @Query("SELECT * FROM game_installations WHERE gameId = :gameId")
    suspend fun getById(gameId: String): GameInstallationEntity?

    @Query("SELECT * FROM game_installations WHERE gameId = :gameId")
    fun observeById(gameId: String): Flow<GameInstallationEntity?>

    @Query("SELECT installedVersion FROM game_installations WHERE gameId = :gameId")
    suspend fun getInstalledVersion(gameId: String): String?

    @Query("SELECT EXISTS(SELECT 1 FROM game_installations WHERE gameId = :gameId AND installedVersion != '' AND installedVersion != 'none')")
    suspend fun isInstalled(gameId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM game_installations WHERE gameId = :gameId AND installedVersion != '' AND installedVersion != 'none')")
    fun observeInstallationStatus(gameId: String): Flow<Boolean>

    @Query("DELETE FROM game_installations WHERE gameId = :gameId")
    suspend fun delete(gameId: String)
}
