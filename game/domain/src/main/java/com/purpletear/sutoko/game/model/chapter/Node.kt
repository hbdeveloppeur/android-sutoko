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
        val isHesitating: Boolean = false,
        val isAutoTiming: Boolean = true,
    ) : Node()

    /**
     * Styling directive: updates the bubble background and text foreground colors of
     * subsequent [Message] nodes until the next [MessageTheme] node. Either color may be
     * null to leave that channel unchanged. Hex format (e.g. "#FF2200").
     */
    @Keep
    data class MessageTheme(
        override val id: String,
        val backgroundColor: String?,
        val foregroundColor: String?,
    ) : Node()

    @Keep
    data class MessageImage(
        override val id: String,
        val imageUrl: String,
        val characterId: Int,
        val assetId: Int? = null,
        val waitMs: Long = 0,
        val seenMs: Long = 0,
    ) : Node()

    /**
     * A full-screen manga page: displays [imageUrl] with [messages] drawn on top as
     * speech-bubble text. [waitMs] is the post-typing delay and [seenMs] the pre-show
     * delay (mapped from authored `duration`/`delay`). Coordinates are percentages of
     * the image (x/y = text center, w = constrained text width); size is in image pixels.
     */
    @Keep
    data class MangaPage(
        override val id: String,
        val imageUrl: String,
        val assetId: Int? = null,
        val messages: List<MangaMessage>,
        val waitMs: Long = 0,
        val seenMs: Long = 0,
    ) : Node() {
        @Keep
        data class MangaMessage(
            val text: String,
            val size: Float,
            val x: Float,
            val y: Float,
            val w: Float,
        )
    }


    @Keep
    data class Info(
        override val id: String,
        val text: String,
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
    data class Background(
        override val id: String,
        val imageUrl: String
    ) : Node()

    @Keep
    data class ConversationModeChange(
        override val id: String,
        val mode: ConversationMode
    ) : Node()

    @Keep
    data class End(
        override val id: String
    ) : Node()

    @Keep
    data class Sound(
        override val id: String,
        val soundUrl: String,
        val loop: Boolean = false
    ) : Node()

    @Keep
    data class MessageVocal(
        override val id: String,
        val audioUrl: String,
        val characterId: Int,
    ) : Node()

    @Keep
    data class Code(
        override val id: String,
        val sentence: String,
    ) : Node() {
        val isIntroStart: Boolean get() = sentence.trim() == "[intro=start]"
        val isIntroEnd: Boolean get() = sentence.trim() == "[intro=end]"
    }

    @Keep
    data class IntroSentence(
        override val id: String,
        val text: String,
        val alignment: IntroAlignment,
        val delayMs: Long = 0,
        val durationMs: Long = 0,
    ) : Node()
}

@Keep
enum class IntroAlignment {
    START,
    END,
    TOP,
    BOTTOM,
    CENTER,
}
