package com.purpletear.ai_conversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.enums.MediaType
import com.purpletear.ai_conversation.domain.model.AvatarBannerPair
import com.purpletear.ai_conversation.domain.model.Media

@Keep
data class AvatarBannerPairMediaDto(
    val id: Int,
    val url: String
)

fun AvatarBannerPairMediaDto.toDomain(type: MediaType): Media {
    return Media(id = id, url = url, typeCode = type.code)
}

@Keep
data class AvatarBannerPairDto(
    val avatar: AvatarBannerPairMediaDto?,
    val banner: AvatarBannerPairMediaDto
)


fun AvatarBannerPairDto.toDomain(): AvatarBannerPair {
    return AvatarBannerPair(
        avatar = avatar?.toDomain(type = MediaType.Avatar),
        banner = banner.toDomain(type = MediaType.Banner)
    )
}