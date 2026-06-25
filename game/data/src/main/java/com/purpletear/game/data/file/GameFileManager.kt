package com.purpletear.game.data.file

interface GameFileManager {
    suspend fun downloadAndExtract(
        gameId: String,
        downloadUrl: String,
        onProgress: suspend (Float) -> Unit,
        legacyId: Int? = null,
    ): String

    suspend fun deleteGame(gameId: String, legacyId: Int? = null)

    fun getInstallPath(gameId: String, legacyId: Int? = null): String
}
