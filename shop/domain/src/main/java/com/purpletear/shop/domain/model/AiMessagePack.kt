package com.purpletear.shop.domain.model

import androidx.annotation.Keep
import fr.sutoko.in_app_purchase_domain.model.AppProductDetails

@Keep
data class AiMessagePack(
    val id: Int,
    val identifier: String,
    val tokensCount: Int,
    val productDetails: AppProductDetails?,
)
