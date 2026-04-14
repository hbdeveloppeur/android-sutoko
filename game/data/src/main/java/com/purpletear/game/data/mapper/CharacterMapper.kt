package com.purpletear.game.data.mapper

import com.purpletear.game.data.local.dto.character.CharacterDto
import com.purpletear.sutoko.game.model.character.Character
import com.purpletear.sutoko.game.model.character.CharacterColor
import java.io.File

/**
 * Maps Character DTOs to domain models.
 */
object CharacterMapper {

    fun CharacterDto.toDomain(charactersDir: File): Character = Character(
        id = id,
        name = name,
        avatar = avatarPath?.resolveAvatarPath(charactersDir),
        isMainCharacter = isMainCharacter ?: false,
        color = CharacterColor(
            startingColor = colorStartingCode ?: "#8E2DE2",
            endingColor = colorEndingCode ?: "#4A00E0",
        ),
    )

    private fun String.resolveAvatarPath(charactersDir: File): String? {
        if (isBlank()) return null
        val file = File(charactersDir, this)
        return if (file.exists()) file.absolutePath else null
    }
}
