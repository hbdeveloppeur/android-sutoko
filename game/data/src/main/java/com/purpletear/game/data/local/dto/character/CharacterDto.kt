package com.purpletear.game.data.local.dto.character

import com.google.gson.annotations.SerializedName

/**
 * DTO for characters.json parsing.
 */
data class CharacterDto(
    val id: Int,
    val name: String,
    @SerializedName("avatarPath")
    val avatarPath: String?,
    @SerializedName("isMainCharacter")
    val isMainCharacter: Boolean? = false,
    @SerializedName("colorStartingCode")
    val colorStartingCode: String?,
    @SerializedName("colorEndingCode")
    val colorEndingCode: String?,
)
