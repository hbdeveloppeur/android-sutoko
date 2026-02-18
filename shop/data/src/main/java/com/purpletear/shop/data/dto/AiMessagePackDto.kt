package com.purpletear.shop.data.dto

import androidx.annotation.Keep
import com.purpletear.shop.domain.model.AiMessagePack

@Keep
data class AiMessagePackDto(
    val id: Int,
    val identifier: String,
    val tokensCount: Int,
    val giftType: String?,
    val giftId: Int?,
    val price: Int,
)


fun AiMessagePackDto.toDomain(): AiMessagePack {
    return AiMessagePack(
        id = id,
        identifier = identifier,
        tokensCount = tokensCount,
        productDetails = null
    )
}