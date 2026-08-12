package com.purpletear.game.presentation.game_play.pacing

import com.purpletear.game.data.infrastructure.SystemTimingScheduler
import com.purpletear.sutoko.game.engine.GameEngine
import com.purpletear.sutoko.game.engine.GameEngineState
import com.purpletear.sutoko.game.model.StoryAdvanceMode
import com.purpletear.sutoko.game.repository.StoryAdvanceModeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Auto-advance pacing: watches the player's [StoryAdvanceMode] and resumes the engine past
 * tap gates on its own when auto-play is on (or when the gate never requires a tap).
 * A player tap before the deadline wins: the state leaves AwaitingTap, the pending job is
 * cancelled, and [GameEngine.advanceOnTap] no-ops otherwise. All jobs run on the ViewModel's
 * scope and use the timing scheduler, so hold-to-pause freezes the countdown too.
 */
class AutoAdvanceController(
    private val gameEngine: GameEngine,
    private val timingScheduler: SystemTimingScheduler,
    storyAdvanceModeRepository: StoryAdvanceModeRepository,
    private val scope: CoroutineScope,
) {
    /** Pending auto-advance past the current tap gate; cancelled as soon as the gate closes. */
    private var autoAdvanceJob: Job? = null

    /** Whether the story advances on its own past tap gates or waits for a player tap. */
    private val advanceMode: StateFlow<StoryAdvanceMode> = storyAdvanceModeRepository.observe()
        .stateIn(scope, SharingStarted.Eagerly, StoryAdvanceMode.AUTO_PLAY)

    /**
     * Starts reacting to mid-game [StoryAdvanceMode] changes applied to a currently parked
     * tap gate: turning AutoPlay off cancels the pending advance (unless the gate never
     * requires a tap); turning it on schedules one if the engine is still waiting.
     */
    fun start() {
        scope.launch {
            advanceMode.collect {
                val state = gameEngine.state.value
                if (state is GameEngineState.AwaitingTap && shouldAutoAdvance(state)) {
                    scheduleAutoAdvance(state)
                } else {
                    autoAdvanceJob?.cancel()
                }
            }
        }
    }

    /** Feeds the current engine state: schedules or cancels the pending auto-advance. */
    fun onEngineState(engineState: GameEngineState) {
        if (engineState is GameEngineState.AwaitingTap && shouldAutoAdvance(engineState)) {
            scheduleAutoAdvance(engineState)
        } else {
            autoAdvanceJob?.cancel()
        }
    }

    /**
     * Whether a parked tap gate resolves on its own: always in auto-play mode, and for
     * gates that declared [GameEngineState.AwaitingTap.requiresTap] = false (e.g. scene
     * transitions), which auto-continue even in click-to-advance mode.
     */
    private fun shouldAutoAdvance(state: GameEngineState.AwaitingTap): Boolean =
        advanceMode.value == StoryAdvanceMode.AUTO_PLAY || !state.requiresTap

    /**
     * Auto-advance driver: resumes the engine once the tap gate's pacing delay has elapsed,
     * so the story progresses without requiring a tap.
     */
    private fun scheduleAutoAdvance(state: GameEngineState.AwaitingTap) {
        autoAdvanceJob?.cancel()
        autoAdvanceJob = scope.launch {
            timingScheduler.delay(state.autoAdvanceAfterMs)
            val current = gameEngine.state.value
            if (current is GameEngineState.AwaitingTap && current.currentNodeId == state.currentNodeId) {
                gameEngine.advanceOnTap()
            }
        }
    }
}
