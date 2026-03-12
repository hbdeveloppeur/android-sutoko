package com.purpletear.game.presentation.smsgame.engine

import androidx.annotation.Keep

/**
 * Enum representing all node types in the game.
 */
@Keep
enum class NodeType {
    START,
    MESSAGE,
    CHAPTER_CHANGE,
    CHOICE,
    CONDITION,
    MEMORY,
    INFO,
    TROPHY,
    SIGNAL,
    BACKGROUND
}
