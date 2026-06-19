package com.purpletear.game.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.game.data.local.entity.GameCatalogEntity

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
    @SerializedName("legacyId") val legacyId: Int?,
    @SerializedName("official") val official: Boolean?,
)

/**
 * Extension function to convert GameDto to Game domain model.
 */
fun GameDto.toDomain(): GameCatalogEntity {
    return GameCatalogEntity(
        id = id,
        version = version,
        isCertified = isCertified,
        price = price,
        skuIdentifiers = skuIdentifiers,
        videoUrl = videoUrl,
        chaptersCount = cachedChaptersCount,
        banner = bannerAsset?.toDomain(),
        logo = logoAsset?.toDomain(),
        metadata = metadata.toDomain(),
        author = author?.toDomain(),
        legacyId = legacyId,
        isOfficial = official ?: false,
    )
}

/**
 * Extension function to convert a list of GameDto to a list of Game domain models.
 */
fun List<GameDto>.toDomain(): List<GameCatalogEntity> {
    return map { it.toDomain() }
}
