package com.purpletear.game_data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.game_data.remote.dto.media.MediaImageDto
import com.purpletear.game_data.remote.dto.media.toDomain
import com.purpletear.sutoko.game.model.Game

/**
 * Data Transfer Object for Game.
 */
@Keep
data class GameDto(
    @SerializedName("id") val id: Int,
    @SerializedName("isPremium") val isPremium: Boolean,
    @SerializedName("menuSoundUrl") val menuSoundUrl: String?,
    @SerializedName("minAppCode") val minAppCode: Int,
    @SerializedName("keywords") val keywords: List<String>,
    @SerializedName("releaseDate") val releaseDate: Long,
    @SerializedName("mediaLogoSquare") val mediaLogoSquare: MediaImageDto,
    @SerializedName("mediaMainBanner") val mediaMainBanner: MediaImageDto,
    @SerializedName("mediaPreviewBackground") val mediaPreviewBackground: MediaImageDto,
    @SerializedName("versionCode") val versionCode: String,
    @SerializedName("metadata") val metadata: GameMetadataDto,
    @SerializedName("price") val coinsPrice: Int?,
    @SerializedName("skuIdentifiers") val skuIdentifiers: List<String>,
    @SerializedName("videoUrl") val videoUrl: String? = null,
    @SerializedName("cachedChaptersCount") val cachedChaptersCount: Int = 0,
)

/**
 * Extension function to convert GameDto to Game domain model.
 */
fun GameDto.toDomain(): Game {
    return Game(
        id = id,
        isPremium = isPremium,
        menuSoundUrl = menuSoundUrl,
        minAppCode = minAppCode,
        keywords = keywords,
        releaseDate = releaseDate,
        mediaLogoSquare = mediaLogoSquare.toDomain(),
        mediaMainBanner = mediaMainBanner.toDomain(),
        mediaPreviewBackground = mediaPreviewBackground.toDomain(),
        versionCode = versionCode,
        price = coinsPrice,
        metadata = metadata.toDomain(),
        skuIdentifiers = skuIdentifiers,
        videoUrl = videoUrl,
        cachedChaptersCount = cachedChaptersCount,
    )
}

/**
 * Extension function to convert a list of GameDto to a list of Game domain models.
 */
fun List<GameDto>.toDomain(): List<Game> {
    return map { it.toDomain() }
}
