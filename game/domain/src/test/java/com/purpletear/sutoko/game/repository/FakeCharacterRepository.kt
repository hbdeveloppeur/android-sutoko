package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.character.Character
import com.purpletear.sutoko.game.model.character.CharacterColor

/**
 * Fake [CharacterRepository] for unit tests.
 * Characters are pre-populated in memory; [preload] is a no-op.
 */
class FakeCharacterRepository(
    private val characters: List<Character> = listOf(
        Character(
            id = 1,
            name = "Main",
            avatar = null,
            isMainCharacter = true,
            color = CharacterColor("#000000", "#000000")
        )
    )
) : CharacterRepository {

    override suspend fun preload(gameId: String) {
        // No-op: characters are already in memory.
    }

    override suspend fun getCharacter(id: Int): Character? =
        characters.find { it.id == id }

    override suspend fun getAll(): List<Character> =
        characters.toList()

    override suspend fun clear() {
        // No-op for tests.
    }
}
