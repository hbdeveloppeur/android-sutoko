package com.purpletear.game.data.file

import com.purpletear.sutoko.game.model.game.GameDownloadState

interface GameFileManager {
    suspend fun downloadAndExtract(
        gameId: String,
        downloadUrl: String,
        onState: suspend (GameDownloadState) -> Unit,
        legacyId: Int? = null,
    ): String

    suspend fun deleteGame(gameId: String, legacyId: Int? = null)

    fun getInstallPath(gameId: String, legacyId: Int? = null): String
}
