package com.purpletear.aiconversation.domain.model

import androidx.annotation.Keep

@Keep
data class AiTokensState(
    val messagesCount: Int,
    val freeTrialAvailable: Boolean,
)