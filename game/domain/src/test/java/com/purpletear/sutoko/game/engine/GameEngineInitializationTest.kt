package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.model.character.Character
import com.purpletear.sutoko.game.model.character.CharacterColor
import com.purpletear.sutoko.game.repository.FakeCharacterRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineInitializationTest {

    @Test
    fun `initialize with multiple main characters - should not throw and become ready`() = runBlocking {
        val characters = listOf(
            Character(id = 7087, name = "lucie", avatar = null, isMainCharacter = true, color = CharacterColor("#8E2DE2", "#4A00E0")),
            Character(id = 7088, name = "mark", avatar = null, isMainCharacter = true, color = CharacterColor("#ff6a88", "#ff99ac")),
            Character(id = 7089, name = "espris", avatar = null, isMainCharacter = true, color = CharacterColor("#f12711", "#f5af19"))
        )
        val engine = createTestGameEngine(characterRepository = FakeCharacterRepository(characters))
        val graph = startOnlyGraph()

        engine.initialize("game-1", graph)

        assertTrue(engine.state.value is GameEngineState.Ready)
    }

    @Test
    fun `initialize with no main character - should not throw and become ready`() = runBlocking {
        val engine = createTestGameEngine(characterRepository = FakeCharacterRepository(emptyList()))
        val graph = startOnlyGraph()

        engine.initialize("game-1", graph)

        assertTrue(engine.state.value is GameEngineState.Ready)
    }
}
