package com.purpletear.sutoko.game.engine

import androidx.annotation.Keep

/**
 * Effects returned by handlers that the engine applies to state.
 * Pure data representation of side effects for predictable behavior.
 *
 * These effects describe WHAT should happen, not HOW.
 * The GameEngine applies these effects to state; presentation layer
 * renders them (messages, animations, sounds, etc.).
 *
 * Effects are wrapped in [HandlerCommand.Emit] commands and returned
 * via [HandlerScript] from [NodeHandler.prepare].
 */
sealed class HandlerEffect {

    /**
     * Add a message to the conversation.
     */
    @Keep
    data class AddMessage(val message: GameMessage) : HandlerEffect()

    /**
     * Remove a message from the conversation by id.
     */
    @Keep
    data class DeleteMessage(val messageId: String) : HandlerEffect()

    /**
     * Replace an existing message in place, preserving its position in the conversation.
     * Used to transition a message's state (e.g. typing indicator -> final text) while keeping
     * a stable identity so the UI can cross-fade instead of remove+add.
     * No-op when [messageId] is not present.
     */
    @Keep
    data class ReplaceMessage(val messageId: String, val message: GameMessage) : HandlerEffect()

    /**
     * Change the background image.
     */
    @Keep
    data class ChangeBackground(val imageUrl: String) : HandlerEffect()

    /**
     * Play typingSound
     */
    data object PlayTypingSound : HandlerEffect()

    /**
     * Play a sound effect or music.
     */
    @Keep
    data class PlaySound(val soundUrl: String, val loop: Boolean = false) : HandlerEffect()

    /**
     * Stop currently playing sound.
     */
    data object StopSound : HandlerEffect()

    /**
     * Show glitch effect on screen for thriller atmosphere.
     */
    @Keep
    data class ShowGlitch(val durationMs: Int = 300) : HandlerEffect()

    /**
     * Send a signal/event to external systems (analytics, telemetry, etc.).
     */
    @Keep
    data class SendSignal(
        val action: String,
        val payload: Map<String, String> = emptyMap()
    ) : HandlerEffect()

    /**
     * Schedule a local notification for later delivery.
     */
    @Keep
    data class ScheduleNotification(
        val title: String,
        val message: String,
        val delayMs: Long
    ) : HandlerEffect()

    /**
     * Load and display an image in the conversation.
     */
    @Keep
    data class LoadImage(val imageUrl: String) : HandlerEffect()

    /**
     * Save a score or achievement remotely.
     */
    @Keep
    data class SaveScore(
        val score: Int,
        val metadata: Map<String, String> = emptyMap()
    ) : HandlerEffect()

    /**
     * Unlock a trophy/achievement.
     */
    @Keep
    data class UnlockTrophy(val trophyId: String) : HandlerEffect()

    /**
     * Show action choices to the player and pause for input.
     */
    @Keep
    data class ShowChoices(
        val choices: List<Choice>
    ) : HandlerEffect() {
        @Keep
        data class Choice(
            val id: String,
            val text: String,
            val nextNodeId: String? = null
        )
    }


    /**
     * Play a vocal/audio message.
     */
    @Keep
    data class PlayVocal(val audioUrl: String) : HandlerEffect()

    /**
     * Change to a different chapter.
     */
    @Keep
    data class ChangeChapter(val chapterCode: String) : HandlerEffect()

    /**
     * Update game memory (variables).
     */
    @Keep
    data class UpdateMemory(
        val key: String,
        val value: String
    ) : HandlerEffect()

    /**
     * Change the conversation display mode (SMS or IRL).
     * Notifies UI to update rendering style.
     */
    @Keep
    data class ChangeConversationMode(
        val mode: String
    ) : HandlerEffect()

    /**
     * Change to a scene configuration.
     * The presentation layer resolves sceneId to the actual asset
     * using SceneRepository.
     */
    @Keep
    data class ChangeScene(
        val sceneId: Int
    ) : HandlerEffect()

    /**
     * A cinematic was triggered at `[intro=start]`. The presentation layer extracts the linear
     * body between [startNodeId] and [endNodeId] and plays it; the engine parks until it is resumed
     * at the successor of the end marker.
     */
    @Keep
    data class EnterCinematic(
        val startNodeId: String,
        val endNodeId: String
    ) : HandlerEffect()

    /**
     * Signals the end of the story/game.
     */
    data object StoryFinished : HandlerEffect()
}
