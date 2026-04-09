package com.purpletear.sutoko.game.model.chapter

import androidx.annotation.Keep

@Keep
sealed class Node {
    abstract val id: String


    @Keep
    data class Start(
        override val id: String,
        val label: String = "Start"
    ) : Node()

    @Keep
    data class Scene(
        override val id: String,
        val sceneId: Int
    ) : Node()

    @Keep
    data class Message(
        override val id: String,
        val text: String,
        val characterId: Int,
        val waitMs: Long = 0,
        val seenMs: Long = 0,
    ) : Node()


    @Keep
    data class Info(
        override val id: String,
        val text: String,
        val seenMs: Long = 0,
    ) : Node()

    @Keep
    data class ChapterChange(
        override val id: String,
        val chapterCode: String
    ) : Node()

    @Keep
    data class Condition(
        override val id: String,
        val expression: String,
        val trueTargetId: String,
        val falseTargetId: String
    ) : Node()

    @Keep
    data class Memory(
        override val id: String,
        val key: String,
        val value: String
    ) : Node()


    @Keep
    data class Trophy(
        override val id: String,
        val trophyId: String
    ) : Node()

    @Keep
    data class Signal(
        override val id: String,
        val action: String,
        val payload: Map<String, String> = emptyMap()
    ) : Node()

    @Keep
    data class Background(
        override val id: String,
        val imageUrl: String
    ) : Node()

    @Keep
    data class ConversationModeChange(
        override val id: String,
        val mode: ConversationMode
    ) : Node()
}
