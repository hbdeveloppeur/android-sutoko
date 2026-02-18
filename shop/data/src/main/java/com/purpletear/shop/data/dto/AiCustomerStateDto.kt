package com.purpletear.shop.data.dto

import androidx.annotation.Keep
import com.purpletear.shop.domain.model.AiCustomerState

@Keep
data class AiCustomerStateDto(
    val count: Int,
    val freeTrialAvailable: Boolean,
    val canWatchAd: Boolean
)

fun AiCustomerStateDto.toDomain() = AiCustomerState(
    messagesCount = count,
    freeTrialAvailable = freeTrialAvailable,
    canWatchAd = canWatchAd
)

