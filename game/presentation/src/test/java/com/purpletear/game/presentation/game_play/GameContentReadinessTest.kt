package com.purpletear.game.presentation.game_play

import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.game.presentation.game_play.state.GameEngineStateUiMapper
import com.purpletear.sutoko.game.engine.GameEngineState
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.scene.BackgroundType
import com.purpletear.sutoko.game.model.scene.Scene
import com.purpletear.sutoko.game.model.scene.SceneConfiguration
import org.junit.Assert.assertEquals
import org.junit.Test

class GameContentReadinessTest {
    @Test
    fun `a chapter awaiting its first choice is ready to reveal`() {
        val state = GameEngineStateUiMapper.map(
            GameUiState(),
            GameEngineState.AwaitingInput(chapterCode = "1A", currentNodeId = "choice"),
        )

        assertEquals(GameContentReadiness.Ready, state.contentReadiness())
    }

    @Test
    fun `choices can arrive before the engine input state`() {
        val state = GameUiState(
            choices = listOf(HandlerEffect.ShowChoices.Choice(id = "choice", text = "Continue")),
        )

        assertEquals(GameContentReadiness.Ready, state.contentReadiness())
    }

    @Test
    fun `loading without content keeps the loading cover`() {
        assertEquals(
            GameContentReadiness.Waiting,
            GameUiState(isLoadingStoryUpdates = true).contentReadiness(),
        )
    }

    @Test
    fun `a scene before the first message is visible content`() {
        val state = GameUiState(
            currentScene = Scene(
                id = 1,
                name = "Opening scene",
                configuration = SceneConfiguration(BackgroundType.COLOR, asset = null),
            ),
        )

        assertEquals(GameContentReadiness.Waiting, state.contentReadiness(sceneReady = false))
        assertEquals(GameContentReadiness.Ready, state.contentReadiness(sceneReady = true))
    }

    @Test
    fun `an engine failure shows recovery even when content already exists`() {
        val state = GameEngineStateUiMapper.map(
            GameUiState(isAwaitingInput = true),
            GameEngineState.Error(message = "Could not load the chapter"),
        )

        assertEquals(GameContentReadiness.Failed, state.contentReadiness(sceneReady = false))
    }

    @Test
    fun `a new engine session clears a previous load failure`() {
        val state = GameEngineStateUiMapper.map(
            GameUiState(hasLoadError = true),
            GameEngineState.Ready(chapterCode = "1A", currentNodeId = "start"),
        )

        assertEquals(GameContentReadiness.Waiting, state.contentReadiness())
    }
}
