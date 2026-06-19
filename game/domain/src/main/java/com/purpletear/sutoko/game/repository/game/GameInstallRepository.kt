package com.purpletear.sutoko.game.repository.game

import com.purpletear.sutoko.game.model.game.GameInstall
import kotlinx.coroutines.flow.Flow

interface GameInstallRepository {
    fun observeInstalls(): Flow<List<GameInstall>>
    fun observeInstall(gameId: String): Flow<GameInstall?>
    fun download(gameId: String, gameDownloadUrl: String, gameVersion: String): Flow<Float>
    fun observeDownloadProgress(gameId: String): Flow<Float?>
    suspend fun deleteGame(gameId: String): Result<Unit>
}