package com.purpletear.game.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.game.model.Game

/**
 * Data Transfer Object for Game.
 */
@Keep
data class GameDto(
    @SerializedName("id") val id: String,
    @SerializedName("version") val version: Int,
    @SerializedName("interactionCount") val interactionCount: Int,
    @SerializedName("downloadCount") val downloadCount: Int,
    @SerializedName("isCertified") val isCertified: Boolean,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("price") val price: Int,
    @SerializedName("skuIdentifiers") val skuIdentifiers: List<String>,
    @SerializedName("videoUrl") val videoUrl: String?,
    @SerializedName("cachedChaptersCount") val cachedChaptersCount: Int,
    @SerializedName("bannerAsset") val bannerAsset: AssetDto?,
    @SerializedName("logoAsset") val logoAsset: AssetDto?,
    @SerializedName("metadata") val metadata: GameMetadataDto,
    @SerializedName("author") val author: AuthorDto?,
)

/**
 * Extension function to convert GameDto to Game domain model.
 */
fun GameDto.toDomain(): Game {
    return Game(
        id = id,
        version = version,
        interactionCount = interactionCount,
        downloadCount = downloadCount,
        isCertified = isCertified,
        status = status,
        createdAt = createdAt,
        price = price,
        skuIdentifiers = skuIdentifiers,
        videoUrl = videoUrl,
        cachedChaptersCount = cachedChaptersCount,
        bannerAsset = bannerAsset?.toDomain(),
        logoAsset = logoAsset?.toDomain(),
        metadata = metadata.toDomain(),
        author = author?.toDomain(),
    )
}

/**
 * Extension function to convert a list of GameDto to a list of Game domain models.
 */
fun List<GameDto>.toDomain(): List<Game> {
    return map { it.toDomain() }
}
