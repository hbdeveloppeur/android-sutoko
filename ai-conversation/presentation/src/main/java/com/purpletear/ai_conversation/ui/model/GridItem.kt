package com.purpletear.ai_conversation.ui.model

import androidx.annotation.Keep

@Keep
data class GridItem(
    val url: String,
    val isSelected: Boolean = false,
    val code: String = url,
    val notificationCount: Int = 0
)
