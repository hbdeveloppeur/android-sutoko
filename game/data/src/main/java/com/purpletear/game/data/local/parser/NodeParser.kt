package com.purpletear.game.data.local.parser

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.purpletear.game.data.local.dto.NodeDataDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.model.chapter.IntroAlignment
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.provider.GamePathProvider

private val gson = Gson()

private fun JsonElement?.toNodeData(): NodeDataDto? {
    return when (this) {
        is JsonObject -> gson.fromJson(this, NodeDataDto::class.java)
        else -> null
    }
}

internal fun parseNode(
    dto: NodeDto,
    gameId: String,
    legacyId: Int?,
    pathProvider: GamePathProvider
): Node? {
    val data = dto.data.toNodeData()

    return when (dto.type) {
        "start" -> Node.Start(
            id = dto.id,
            label = data?.label ?: "Start"
        )

        "message" -> Node.Message(
            id = dto.id,
            // Blank text is tolerated: choice hubs are kept by GraphCompactor and the
            // runtime engine skips empty messages.
            text = data?.text.orEmpty(),
            characterId = data?.characterId ?: -1,
            waitMs = data?.wait ?: 0,
            seenMs = data?.seen ?: 0,
            isHesitating = data?.isHesitating ?: false
        )

        "message-theme" -> Node.MessageTheme(
            id = dto.id,
            backgroundColor = data?.backgroundColor?.trim()?.takeIf { it.isNotEmpty() },
            foregroundColor = data?.foregroundColor?.trim()?.takeIf { it.isNotEmpty() }
        )

        // Legacy app: a choice_action phrase rendered inline icon buttons the player
        // tapped to continue (see legacy Conversation.getChoicesAction). Canvas exports
        // flatten it to a linear node (N-in/1-out, no icons, no branch) carrying the
        // player's action label, so it maps to a message from the main character and
        // keeps the standard tap-to-advance beat.
        "choice-action" -> Node.Message(
            id = dto.id,
            text = data?.text.orEmpty(),
            characterId = data?.characterId ?: -1,
            waitMs = data?.wait ?: 0,
            seenMs = data?.seen ?: 0,
            isHesitating = data?.isHesitating ?: false,
            isAutoTiming = data?.isAutoTiming ?: true
        )

        "message-image" -> {
            val imagePath = data?.storagePath ?: data?.image
            require(imagePath != null) { "message-image node ${dto.id} missing storagePath or image" }
            Node.MessageImage(
                id = dto.id,
                imageUrl = resolveImagePath(imagePath, gameId, legacyId, pathProvider),
                characterId = data?.characterId ?: -1,
                waitMs = data?.wait ?: 0,
                seenMs = data?.seen ?: 0
            )
        }

        "chapter-change" -> {
            val id = dto.id
            val chapterCode = data?.chapterCode
            require(id.isNotBlank()) { "chapter-change node missing id" }
            require(!chapterCode.isNullOrBlank()) { "chapter-change node $id missing chapterCode" }
            Node.ChapterChange(
                id = id,
                chapterCode = chapterCode
            )
        }

        "scene-node" -> Node.Scene(
            id = dto.id,
            sceneId = requireNotNull(data?.sceneId) { "scene-node ${dto.id} missing sceneId" }
        )

        "condition" -> Node.Condition(
            id = dto.id,
            expression = requireNotNull(data?.expression) { "condition node ${dto.id} missing expression" }
        )

        "memory", "memory-save-node" -> {
            val id = dto.id
            require(data?.memory != null) { "memory node missing key memory" }
            val memory = data.memory
            Node.Memory(
                id = id,
                key = memory.key,
                value = memory.value
            )
        }

        "memory-condition-node" -> {
            require(data?.memory != null) { "memory-condition-node ${dto.id} memory is null" }
            val memory = data.memory

            // Tolerate canvas exports missing expectedValue (one malformed node must not
            // kill the whole chapter): fall back to the memory's initial value.
            val expectedValue = data.expectedValue ?: memory.value
            Node.Condition(
                id = dto.id,
                expression = "${memory.key} == $expectedValue"
            )
        }

        "narration" -> {
            // Blank narrations are bypassed upstream by GraphCompactor; this require is an invariant guard.
            val id = dto.id
            val text = data?.text
            require(id.isNotBlank()) { "narration node missing id" }
            require(!text.isNullOrBlank()) { "narration node $id missing text" }
            Node.Info(
                id = id, text = text
            )
        }

        "trophy" -> Node.Trophy(
            id = dto.id,
            trophyId = requireNotNull(data?.trophyId) { "trophy node ${dto.id} missing trophyId" }
        )

        "background" -> Node.Background(
            id = dto.id,
            imageUrl = data?.imageUrl ?: ""
        )

        "end" -> Node.End(
            id = dto.id
        )

        "sound" -> {
            val storagePath =
                requireNotNull(data?.storagePath) { "sound node ${dto.id} missing storagePath" }
            val id = dto.id
            val loop = data.isLooping ?: false
            Node.Sound(
                id = id,
                soundUrl = resolveSoundPath(storagePath, gameId, legacyId, pathProvider),
                loop = loop,
                volume = data.volume?.coerceIn(0f, 1f) ?: 1f,
                delayMs = data.delay?.takeIf { it >= 0 } ?: 0
            )
        }

        "stop-sound" -> {
            // A stop-sound without a target is meaningless: drop it like any unknown node.
            val targetNodeId = data?.targetNodeId?.trim()?.takeIf { it.isNotEmpty() }
                ?: return null
            Node.StopSound(
                id = dto.id,
                targetNodeId = targetNodeId
            )
        }

        "message-vocal" -> {
            val storagePath =
                requireNotNull(data?.storagePath) { "message-vocal node ${dto.id} missing storagePath" }
            val characterId =
                requireNotNull(data.characterId) { "message-vocal node ${dto.id} missing characterId" }

            Node.MessageVocal(
                id = dto.id,
                audioUrl = resolveSoundPath(storagePath, gameId, legacyId, pathProvider),
                characterId = characterId
            )
        }

        "message-audio-dialogue" -> {
            val storagePath =
                requireNotNull(data?.storagePath) { "message-audio-dialogue node ${dto.id} missing storagePath" }
            val characterId =
                requireNotNull(data.characterId) { "message-audio-dialogue node ${dto.id} missing characterId" }
            val text =
                requireNotNull(data.text) { "message-audio-dialogue node ${dto.id} missing text" }

            Node.MessageAudioDialogue(
                id = dto.id,
                audioUrl = resolveSoundPath(storagePath, gameId, legacyId, pathProvider),
                characterId = characterId,
                text = text
            )
        }

        "code-message" -> Node.Code(
            id = dto.id,
            sentence = data?.text?.trim().orEmpty()
        )

        "intro-sentence" -> Node.IntroSentence(
            id = dto.id,
            // Blank intro-sentences are bypassed upstream by GraphCompactor; invariant guard.
            text = requireNotNull(data?.text?.takeIf { it.isNotBlank() }) {
                "intro-sentence node ${dto.id} missing text"
            },
            alignment = parseIntroAlignment(data?.alignment, dto.id),
            delayMs = data?.delay ?: 0,
            durationMs = data?.duration ?: 0
        )

        "manga-page" -> parseMangaPage(dto, data, gameId, legacyId, pathProvider)

        "visual-novel" -> parseVisualNovel(dto, data, gameId, legacyId, pathProvider)

        "fake-notification" -> {
            val imagePath = data?.storagePath ?: data?.image
            require(imagePath != null) { "message-notification node ${dto.id} missing storagePath or image" }

            Node.FakeNotification(
                id = dto.id,
                title = data?.title?.trim().orEmpty(),
                subtitle = data?.subtitle?.trim().orEmpty(),
                actionText = data?.actionText?.trim().orEmpty(),
                imageUrl = resolveImagePath(imagePath, gameId, legacyId, pathProvider),
                characterId = data?.characterId,
                delayMs = data?.delay ?: 0,
                durationMs = data?.duration ?: 0,
                isAutoTiming = data?.isAutoTiming ?: true,
                isHesitating = data?.isHesitating ?: false,
            )
        }

        else -> null
    }
}

private fun parseIntroAlignment(raw: String?, nodeId: String): IntroAlignment {
    if (raw.isNullOrBlank()) return IntroAlignment.CENTER
    return when (raw.trim().lowercase()) {
        "start" -> IntroAlignment.START
        "end" -> IntroAlignment.END
        "top" -> IntroAlignment.TOP
        "bottom" -> IntroAlignment.BOTTOM
        "center" -> IntroAlignment.CENTER
        else -> throw IllegalArgumentException(
            "intro-sentence node $nodeId has unknown alignment '$raw' " +
                    "(expected start|end|top|bottom|center)"
        )
    }
}
