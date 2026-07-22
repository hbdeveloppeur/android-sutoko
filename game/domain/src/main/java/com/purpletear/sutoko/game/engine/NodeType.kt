package com.purpletear.sutoko.game.engine

import androidx.annotation.Keep

/**
 * Enumeration of all node types in the game engine.
 */
@Keep
enum class NodeType {
    START,
    MESSAGE,
    MESSAGE_THEME,
    MESSAGE_IMAGE,
    MANGA_PAGE,
    CHAPTER_CHANGE,
    CONDITION,
    SCENE,
    MEMORY,
    INFO,
    TROPHY,
    BACKGROUND,
    CONVERSATION_MODE_CHANGE,
    END,
    SOUND,
    MESSAGE_VOCAL,
    CODE,
    INTRO_SENTENCE
}
