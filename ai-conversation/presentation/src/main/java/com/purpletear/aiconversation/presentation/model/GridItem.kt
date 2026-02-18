package com.purpletear.aiconversation.presentation.model

import androidx.annotation.Keep

@Keep
data class GridItem(
    val url: String,
    val isSelected: Boolean = false,
    val code: String = url,
    val notificationCount: Int = 0
)
