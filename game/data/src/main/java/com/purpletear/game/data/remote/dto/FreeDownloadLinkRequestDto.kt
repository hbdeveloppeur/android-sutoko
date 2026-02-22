package com.purpletear.game.data.remote.dto

import androidx.annotation.Keep

/**
 * Data Transfer Object for requesting a game download link.
 *
 * @property gameId The ID of the game to download
 */
@Keep
data class FreeDownloadLinkRequestDto(
    val gameId: String
)
