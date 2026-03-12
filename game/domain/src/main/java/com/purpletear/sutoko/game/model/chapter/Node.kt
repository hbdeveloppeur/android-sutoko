package com.purpletear.sutoko.game.model.chapter

import androidx.annotation.Keep

@Keep
sealed class Node {
    abstract val id: String
    abstract val position: Position

    @Keep
    data class Position(val x: Float, val y: Float)

    @Keep
    data class Start(
        override val id: String,
        override val position: Position,
        val label: String = "Start"
    ) : Node()

    @Keep
    data class Message(
        override val id: String,
        override val position: Position,
        val text: String,
        val characterId: Int,
        val waitMs: Long = 0,
        val seenMs: Long = 0
    ) : Node()

    @Keep
    data class ChapterChange(
        override val id: String,
        override val position: Position,
        val chapterCode: String
    ) : Node()

    @Keep
    data class Choice(
        override val id: String,
        override val position: Position,
        val options: List<ChoiceOption>
    ) : Node()

    @Keep
    data class ChoiceOption(
        val text: String,
        val targetNodeId: String
    )

    @Keep
    data class Condition(
        override val id: String,
        override val position: Position,
        val expression: String,
        val trueTargetId: String,
        val falseTargetId: String
    ) : Node()

    @Keep
    data class Memory(
        override val id: String,
        override val position: Position,
        val key: String,
        val value: String
    ) : Node()

    @Keep
    data class Info(
        override val id: String,
        override val position: Position,
        val text: String
    ) : Node()

    @Keep
    data class Trophy(
        override val id: String,
        override val position: Position,
        val trophyId: String
    ) : Node()

    @Keep
    data class Signal(
        override val id: String,
        override val position: Position,
        val action: String,
        val payload: Map<String, String> = emptyMap()
    ) : Node()

    @Keep
    data class Background(
        override val id: String,
        override val position: Position,
        val imageUrl: String
    ) : Node()
}
