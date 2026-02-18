package com.purpletear.game.data.remote.dto

import androidx.annotation.Keep

/**
 * Data Transfer Object for the response of a game download link request.
 *
 * @property link The download link for the requested game
 */
@Keep
data class DownloadLinkResponseDto(
    val link: String
)