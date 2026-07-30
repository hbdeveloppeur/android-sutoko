package com.purpletear.game.presentation.game_play.state

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.model.character.Character
import com.purpletear.sutoko.game.model.scene.Scene

/**
 * UI state for the game play screen.
 * Represents the current state of the game session including messages, choices, and input status.
 */
@Keep
data class GameUiState(
    val gameId: String? = null,
    val chapterCode: String? = null,
    val messages: List<GameMessage> = emptyList(),
    val choices: List<HandlerEffect.ShowChoices.Choice> = emptyList(),
    val isAwaitingInput: Boolean = false,
    val isChoicesRevealed: Boolean = false,
    val currentScene: Scene? = null,
    val characters: Map<Int, Character> = emptyMap(),
    /**
     * Ids of the characters displayed on the right side for the current chapter
     * (`Chapter.rightSideCharacterIds`). Empty when the chapter declares no layout:
     * the screen then falls back to the legacy main-character rule.
     */
    val rightSideCharacterIds: Set<Int> = emptySet(),
    val currentVocalUrl: String? = null,
    val isVocalPlaying: Boolean = false,
    val vocalProgress: Float = 0f,
    val isLoadingStoryUpdates: Boolean = false,
    val isTrial: Boolean = false,
    val isNextChapterAvailable: Boolean = true,
    val nextChapterReleaseDate: Long? = null,
    val gameLogoUrl: String? = null,
    val showNextChapterButton: Boolean = true,
    @StringRes val nextChapterTitleRes: Int? = null,
    val cinematicBody: List<Node> = emptyList(),
    val isCinematicActive: Boolean = false,
    val isMangaActive: Boolean = false,
    val isChoicesDarkMode: Boolean = true,
    val isHoldPaused: Boolean = false,
    val fakeNotification: FakeNotificationUi? = null,
)

/**
 * Ephemeral, non-clickable fake system notification displayed over the conversation.
 * [avatarPath] prefers the character avatar when loaded, falling back to the authored image.
 */
@Keep
data class FakeNotificationUi(
    val title: String,
    val subtitle: String,
    val actionText: String,
    val avatarPath: String?,
    val durationMs: Long,
)
