package com.purpletear.game.data.remote.dto

import androidx.annotation.Keep

/**
 * Data Transfer Object for requesting a game download link.
 *
 * @property userId The ID of the user requesting the download
 * @property userToken The token of the user requesting the download
 * @property gameId The ID of the game to download
 */
@Keep
data class DownloadLinkRequestDto(
    val userId: String,
    val userToken: String,
    val gameId: String
)
