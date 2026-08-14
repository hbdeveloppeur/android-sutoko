package com.purpletear.sutoko.game.model

/**
 * How the story progresses past narrative messages.
 *
 * [AUTO_PLAY]: the story advances on its own after a short reading pause.
 * [CLICK_TO_ADVANCE] parks on each message until the player taps the screen.
 */
enum class StoryAdvanceMode {
    AUTO_PLAY,
    CLICK_TO_ADVANCE;

    companion object {
        /**
         * Mode applied when the player never picked one: official stories wait for a tap,
         * user stories play on their own.
         */
        fun defaultFor(isOfficial: Boolean): StoryAdvanceMode =
            if (isOfficial) CLICK_TO_ADVANCE else AUTO_PLAY
    }
}
