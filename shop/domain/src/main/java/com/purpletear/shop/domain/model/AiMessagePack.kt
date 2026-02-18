package com.purpletear.shop.domain.model

import androidx.annotation.Keep
import fr.sutoko.inapppurchase.domain.model.AppProductDetails

@Keep
data class AiMessagePack(
    val id: Int,
    val identifier: String,
    val tokensCount: Int,
    val productDetails: AppProductDetails?,
)
