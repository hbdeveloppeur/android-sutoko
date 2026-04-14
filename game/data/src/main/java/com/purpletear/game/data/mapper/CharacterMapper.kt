package com.purpletear.game.data.mapper

import com.purpletear.game.data.local.dto.character.CharacterDto
import com.purpletear.sutoko.game.model.character.Character
import com.purpletear.sutoko.game.model.character.CharacterColor

/**
 * Maps Character DTOs to domain models.
 */
object CharacterMapper {

    fun CharacterDto.toDomain(): Character = Character(
        id = id,
        name = name,
        avatar = avatarPath,
        isMainCharacter = isMainCharacter ?: false,
        color = CharacterColor(
            startingColor = colorStartingCode ?: "#8E2DE2",
            endingColor = colorEndingCode ?: "#4A00E0",
        ),
    )
}
