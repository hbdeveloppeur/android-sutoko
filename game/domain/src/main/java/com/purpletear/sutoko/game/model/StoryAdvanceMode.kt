package com.purpletear.sutoko.game.model

/**
 * How the story progresses past narrative messages.
 *
 * [AUTO_PLAY] is the default: the story advances on its own after a short reading pause.
 * [CLICK_TO_ADVANCE] parks on each message until the player taps the screen.
 */
enum class StoryAdvanceMode {
    AUTO_PLAY,
    CLICK_TO_ADVANCE,
}
