package com.purpletear.shop.domain.model

import androidx.annotation.Keep

@Keep
data class AiCustomerState(
    val messagesCount: Int,
    val freeTrialAvailable: Boolean,
    val canWatchAd: Boolean
)