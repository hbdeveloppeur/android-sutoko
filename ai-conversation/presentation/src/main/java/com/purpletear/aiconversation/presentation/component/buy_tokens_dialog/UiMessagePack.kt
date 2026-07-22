package com.purpletear.aiconversation.presentation.component.buy_tokens_dialog

import com.purpletear.aiconversation.domain.model.AiMessagePack
import fr.sutoko.inapppurchase.application.domain.model.Product
import androidx.annotation.Keep

@Keep
data class UiMessagePack(
    val id: Int,
    val identifier: String,
    val tokensCount: Int,
    val price: String?,
) {
    constructor(pack: AiMessagePack, billingDetails: Product?) : this(
        id = pack.id,
        identifier = pack.identifier,
        tokensCount = pack.tokensCount,
        price = billingDetails?.formattedPrice,
    )
}
