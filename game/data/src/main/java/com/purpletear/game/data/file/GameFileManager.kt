package com.purpletear.game.data.file

interface GameFileManager {
    suspend fun downloadAndExtract(
        gameId: String,
        downloadUrl: String,
        onProgress: suspend (Float) -> Unit
    ): String

    suspend fun deleteGame(gameId: String)

    fun getInstallPath(gameId: String): String
}
