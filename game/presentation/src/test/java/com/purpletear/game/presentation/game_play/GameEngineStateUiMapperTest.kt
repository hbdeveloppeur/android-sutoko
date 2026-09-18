package com.purpletear.game.presentation.game_play

import com.purpletear.game.presentation.game_play.state.GameEngineStateUiMapper
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.sutoko.game.engine.GameEngineState
import com.purpletear.sutoko.game.engine.HandlerEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineStateUiMapperTest {

    @Test
    fun `manual arrival reveals choices even after intermediate playing state`() {
        val choices = listOf(HandlerEffect.ShowChoices.Choice(id = "a", text = "Yes"))
        val playing = GameEngineStateUiMapper.map(
            GameUiState(), GameEngineState.Playing("1A", "message")
        )
        val mapped = GameEngineStateUiMapper.map(
            playing, GameEngineState.AwaitingInput("1A", "hub", choices, isUserInitiated = true)
        )
        assertEquals(choices, mapped.choices)
        assertTrue(mapped.isChoicesRevealed)
    }

    @Test
    fun `automatic arrival keeps choices hidden`() {
        val mapped = GameEngineStateUiMapper.map(
            GameUiState(isChoicesRevealed = true),
            GameEngineState.AwaitingInput("1A", "hub")
        )
        assertFalse(mapped.isChoicesRevealed)
    }

    @Test
    fun `repeated input state preserves explicit hide and reveal`() {
        val input = GameEngineState.AwaitingInput("1A", "hub", isUserInitiated = true)
        val arrived = GameEngineStateUiMapper.map(GameUiState(), input)
        val hidden = arrived.copy(isChoicesRevealed = false)
        assertFalse(GameEngineStateUiMapper.map(hidden, input).isChoicesRevealed)
        val revealed = hidden.copy(isChoicesRevealed = true)
        assertTrue(GameEngineStateUiMapper.map(revealed, input).isChoicesRevealed)
    }

    @Test
    fun `new choice hub does not inherit previous reveal`() {
        val previous = GameEngineStateUiMapper.map(
            GameUiState(), GameEngineState.AwaitingInput("1A", "hub1", isUserInitiated = true)
        )
        val mapped = GameEngineStateUiMapper.map(previous, GameEngineState.AwaitingInput("1A", "hub2"))
        assertFalse(mapped.isChoicesRevealed)
    }

    @Test
    fun `awaiting input enables input and disables tap`() {
        val state = GameUiState(isAwaitingTap = true)

        val mapped = GameEngineStateUiMapper.map(
            state,
            GameEngineState.AwaitingInput(chapterCode = "1A", currentNodeId = "choice")
        )

        assertTrue(mapped.isAwaitingInput)
        assertFalse(mapped.isAwaitingTap)
    }

    @Test
    fun `awaiting tap enables tap and disables input`() {
        val state = GameUiState(isAwaitingInput = true)

        val mapped = GameEngineStateUiMapper.map(
            state,
            GameEngineState.AwaitingTap(
                chapterCode = "1A",
                currentNodeId = "message",
                autoAdvanceAfterMs = 1000L
            )
        )

        assertTrue(mapped.isAwaitingTap)
        assertFalse(mapped.isAwaitingInput)
    }

    @Test
    fun `awaiting manga dismissal activates manga without touching other flags`() {
        val state = GameUiState(isAwaitingInput = true, isAwaitingTap = true)

        val mapped = GameEngineStateUiMapper.map(
            state,
            GameEngineState.AwaitingMangaDismissal(chapterCode = "1A", currentNodeId = "page")
        )

        assertTrue(mapped.isMangaActive)
        assertTrue(mapped.isAwaitingInput)
        assertTrue(mapped.isAwaitingTap)
    }

    @Test
    fun `awaiting visual novel dismissal clears input gates only`() {
        val state = GameUiState(
            isAwaitingInput = true,
            isAwaitingTap = true,
            isMangaActive = true
        )

        val mapped = GameEngineStateUiMapper.map(
            state,
            GameEngineState.AwaitingVisualNovelDismissal(chapterCode = "1A", currentNodeId = "novel")
        )

        assertFalse(mapped.isAwaitingInput)
        assertFalse(mapped.isAwaitingTap)
        assertTrue(mapped.isMangaActive)
    }

    @Test
    fun `non waiting states reset input choices and manga`() {
        val choice = HandlerEffect.ShowChoices.Choice(id = "choice", text = "Continue")
        val state = GameUiState(
            isAwaitingInput = true,
            isAwaitingTap = true,
            choices = listOf(choice),
            isChoicesRevealed = true,
            isMangaActive = true
        )
        val states = listOf(
            GameEngineState.Playing(chapterCode = "1A", currentNodeId = "message"),
            GameEngineState.Ready(chapterCode = "1A", currentNodeId = "start"),
            GameEngineState.Idle,
            GameEngineState.ChapterFinished(chapterCode = "1A"),
            GameEngineState.Error(message = "error")
        )

        states.forEach { engineState ->
            val mapped = GameEngineStateUiMapper.map(state, engineState)

            assertFalse(mapped.isAwaitingInput)
            assertFalse(mapped.isAwaitingTap)
            assertEquals(emptyList<HandlerEffect.ShowChoices.Choice>(), mapped.choices)
            assertFalse(mapped.isChoicesRevealed)
            assertFalse(mapped.isMangaActive)
        }
    }
}
