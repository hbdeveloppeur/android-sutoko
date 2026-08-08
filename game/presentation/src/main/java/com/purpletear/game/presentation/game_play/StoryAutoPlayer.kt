package com.purpletear.game.presentation.game_play

import android.util.Log
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.sutoko.game.engine.GameMessageType
import com.purpletear.sutoko.game.engine.HandlerEffect
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val KIMI_TAG = "KIMI"

/**
 * Debug-only auto-driver for an SMS story session.
 *
 * When enabled, observes [GameUiState] and submits the first available choice, advances
 * tap-to-continue points, dismisses manga pages and visual novel overlays, skips cinematics,
 * and clicks through to the next chapter. It is intended for Kimi-cli / QA automation and is a no-op in release
 * builds.
 *
 * Reactions are plain sequential ifs (not a `when`): states such as awaiting-tap and
 * awaiting-input are not mutually exclusive with the reset conditions, so branch order in
 * a `when` would silently swallow reactions.
 *
 * Reactions are level-triggered (no edge detection): the ViewModel no-ops spurious calls,
 * and edge flags get stuck when the engine parks again without emitting an intermediate
 * inactive state.
 */
internal class StoryAutoPlayer(
    private val uiState: StateFlow<GameUiState>,
    private val viewModel: GameEngineViewModel,
) {

    private var lastRevealedChoiceSet: List<HandlerEffect.ShowChoices.Choice> = emptyList()
    private var lastChapterEndMessageId: String? = null
    private var lastGameId: String? = null
    private var lastChapterCode: String? = null

    fun start() {
        if (!BuildConfig.DEBUG) return
        logKimi("AUTO_PLAY_ENABLED")
        viewModel.viewModelScope.launch {
            uiState.collect { state -> act(state) }
        }
    }

    private suspend fun act(state: GameUiState) {
        if (state.gameId != null && state.gameId != lastGameId) {
            lastGameId = state.gameId
            logKimi("STORY_STARTED gameId=${state.gameId} chapterCode=${state.chapterCode.orEmpty()}")
        }
        if (state.chapterCode != null && state.chapterCode != lastChapterCode) {
            lastChapterCode = state.chapterCode
            logKimi("CHAPTER_STARTED chapterCode=${state.chapterCode}")
        }

        reactToChoices(state)
        reactToTap(state)
        reactToManga(state)
        reactToVisualNovel(state)
        reactToCinematic(state)
        reactToChapterEnd(state)
    }

    private suspend fun reactToChoices(state: GameUiState) {
        if (!state.isAwaitingInput || state.choices.isEmpty()) {
            lastRevealedChoiceSet = emptyList()
            return
        }
        if (!state.isChoicesRevealed) {
            logKimi("CHOICES_REVEALED count=${state.choices.size}")
            viewModel.onRevealChoicesClicked()
            return
        }
        if (lastRevealedChoiceSet != state.choices) {
            val choice = state.choices.first()
            lastRevealedChoiceSet = state.choices
            logKimi("CHOICE_SELECTED id=${choice.id} nextNodeId=${choice.nextNodeId.orEmpty()} text=\"${choice.text}\"")
            delay(AUTO_PLAY_CONTINUE_DELAY_MS)
            viewModel.onChoiceSelected(choice)
        }
    }

    private suspend fun reactToTap(state: GameUiState) {
        if (!state.isAwaitingTap) return
        delay(AUTO_PLAY_CONTINUE_DELAY_MS)
        logKimi("TAP_ADVANCED chapterCode=${state.chapterCode.orEmpty()}")
        viewModel.onAdvanceOnTap()
    }

    private suspend fun reactToManga(state: GameUiState) {
        if (!state.isMangaActive) return
        delay(AUTO_PLAY_CONTINUE_DELAY_MS)
        logKimi("MANGA_DISMISSED chapterCode=${state.chapterCode.orEmpty()}")
        viewModel.onMangaPageDismissed()
    }

    private suspend fun reactToVisualNovel(state: GameUiState) {
        if (state.visualNovel == null) return
        delay(AUTO_PLAY_CONTINUE_DELAY_MS)
        logKimi("VISUAL_NOVEL_DISMISSED chapterCode=${state.chapterCode.orEmpty()}")
        viewModel.onVisualNovelDismissed()
    }

    private suspend fun reactToCinematic(state: GameUiState) {
        if (!state.isCinematicActive) return
        delay(AUTO_PLAY_CONTINUE_DELAY_MS)
        logKimi("CINEMATIC_SKIPPED chapterCode=${state.chapterCode.orEmpty()}")
        viewModel.onCinematicFinished()
    }

    private suspend fun reactToChapterEnd(state: GameUiState) {
        val chapterEnd = state.messages.firstOrNull { it.type == GameMessageType.ChapterEnd }
            ?: return
        if (chapterEnd.id == lastChapterEndMessageId) return
        // The availability check is launched together with the ChapterEnd message; deciding
        // on the unresolved state would race it (isNextChapterAvailable defaults to true).
        if (!state.isNextChapterAvailabilityResolved) return
        lastChapterEndMessageId = chapterEnd.id
        if (state.isNextChapterAvailable && state.showNextChapterButton) {
            logKimi("CHAPTER_FINISHED chapterCode=${state.chapterCode.orEmpty()} nextChapterAvailable=true")
            delay(AUTO_PLAY_CHAPTER_DELAY_MS)
            viewModel.onNextChapterClicked()
        } else {
            logKimi("STORY_COMPLETED chapterCode=${state.chapterCode.orEmpty()} nextChapterAvailable=false")
        }
    }

    private fun logKimi(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(KIMI_TAG, "SutokoGameEngine [KIMI] $message")
        }
    }

    private companion object {
        const val AUTO_PLAY_CONTINUE_DELAY_MS = 300L
        const val AUTO_PLAY_CHAPTER_DELAY_MS = 800L
    }
}
