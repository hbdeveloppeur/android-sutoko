package com.purpletear.sutoko.game.model.character

import androidx.annotation.Keep

@Keep
data class Character(
    val id: Int,
    val name: String,
    val avatar: String?,
    val isMainCharacter: Boolean,
    val color: CharacterColor,
)

@Keep
data class CharacterColor(
    val startingColor: String,
    val endingColor: String,
)
