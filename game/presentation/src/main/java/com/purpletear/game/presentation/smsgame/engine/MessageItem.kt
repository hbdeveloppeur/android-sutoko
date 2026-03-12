package com.purpletear.game.presentation.smsgame.engine

import androidx.annotation.Keep

@Keep
data class MessageItem(
    val id: String,
    val text: String,
    val characterId: Int,
    val isMainCharacter: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
