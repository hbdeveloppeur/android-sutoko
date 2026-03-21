package com.purpletear.sutoko.game.engine

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
    data class AddMessage(val message: GameMessage) : HandlerEffect()

    /**
     * Update the last message's status.
     * Used for transitioning from TYPING to SENT.
     */
    data class UpdateLastMessageStatus(val status: GameMessage.Status) : HandlerEffect()

    /**
     * Change the background image.
     */
    data class ChangeBackground(val imageUrl: String) : HandlerEffect()

    /**
     * Play a sound effect or music.
     */
    data class PlaySound(val soundUrl: String, val loop: Boolean = false) : HandlerEffect()

    /**
     * Stop currently playing sound.
     */
    data object StopSound : HandlerEffect()

    /**
     * Show glitch effect on screen for thriller atmosphere.
     */
    data class ShowGlitch(val durationMs: Int = 300) : HandlerEffect()

    /**
     * Send a signal/event to external systems (analytics, telemetry, etc.).
     */
    data class SendSignal(
        val action: String,
        val payload: Map<String, String> = emptyMap()
    ) : HandlerEffect()

    /**
     * Schedule a local notification for later delivery.
     */
    data class ScheduleNotification(
        val title: String,
        val message: String,
        val delayMs: Long
    ) : HandlerEffect()

    /**
     * Load and display an image in the conversation.
     */
    data class LoadImage(val imageUrl: String) : HandlerEffect()

    /**
     * Save a score or achievement remotely.
     */
    data class SaveScore(
        val score: Int,
        val metadata: Map<String, String> = emptyMap()
    ) : HandlerEffect()

    /**
     * Unlock a trophy/achievement.
     */
    data class UnlockTrophy(val trophyId: String) : HandlerEffect()

    /**
     * Show action choices to the player and pause for input.
     */
    data class ShowChoices(
        val choices: List<Choice>
    ) : HandlerEffect() {
        data class Choice(
            val id: String,
            val text: String,
            val nextNodeId: String? = null
        )
    }


    /**
     * Play a vocal/audio message.
     */
    data class PlayVocal(val audioUrl: String) : HandlerEffect()

    /**
     * Change to a different chapter.
     */
    data class ChangeChapter(val chapterCode: String) : HandlerEffect()

    /**
     * Update game memory (variables).
     */
    data class UpdateMemory(
        val key: String,
        val value: String
    ) : HandlerEffect()
}
