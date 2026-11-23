package com.purpletear.sutoko.game.engine

import androidx.annotation.Keep
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.MemoryEntry
import com.purpletear.sutoko.game.model.chapter.Node
import java.security.MessageDigest

/** A durable, stable reading boundary. It never captures a partially executed script. */
@Keep
data class GameCheckpoint(
    val chapterCode: String,
    val graphFingerprint: String,
    val nodeId: String,
    val gate: Gate,
    val memories: Map<String, MemoryEntry>,
    val messages: List<GameMessage>,
    val previousNodeId: String? = null,
    val choices: List<HandlerEffect.ShowChoices.Choice> = emptyList(),
    val autoAdvanceAfterMs: Long = 0,
    val requiresTap: Boolean = true,
    val sceneId: Int? = null,
    val loopingSounds: List<HandlerEffect.PlaySound> = emptyList(),
    val cinematicEndNodeId: String? = null,
    val selectedChoiceNodeId: String? = null,
    val formatVersion: Int = 1,
) {
    @Keep
    enum class Gate { NODE, TAP, CHOICE, MANGA, VISUAL_NOVEL, CINEMATIC, FINISHED }

    fun isCompatible(graph: ChapterGraph, fingerprint: String): Boolean = runCatching {
        formatVersion == 1 && chapterCode.equals(graph.chapterCode, ignoreCase = true) &&
            graphFingerprint == fingerprint && graph.getNode(nodeId) != null &&
            (previousNodeId == null || graph.getNode(previousNodeId) != null) &&
            memories.all { (key, entry) -> key.isNotBlank() && entry.value.length >= 0 && entry.chapterNumber in 1..graph.chapterNumber } &&
            messages.all { it.id.isNotBlank() && it.type != GameMessageType.Typing } &&
            autoAdvanceAfterMs >= 0 && loopingSounds.all { it.loop && it.soundUrl.isNotBlank() } &&
            when (gate) {
                Gate.NODE -> selectedChoiceNodeId == nodeId && graph.getNode(nodeId) is Node.Message
                Gate.CHOICE -> choices.isNotEmpty() && choices.all { it.nextNodeId != null && graph.getNode(it.nextNodeId) is Node.Message }
                Gate.MANGA -> graph.getNode(nodeId) is Node.MangaPage
                Gate.VISUAL_NOVEL -> graph.getNode(nodeId) is Node.VisualNovel
                Gate.CINEMATIC -> (graph.getNode(nodeId) as? Node.Code)?.isIntroStart == true &&
                    (cinematicEndNodeId?.let(graph::getNode) as? Node.Code)?.isIntroEnd == true
                Gate.TAP, Gate.FINISHED -> true
            }
    }.getOrDefault(false)
}

/** Includes authored text, edges and media paths: changed/localized exports must not reuse an old gate. */
fun ChapterGraph.checkpointFingerprint(): String = MessageDigest.getInstance("SHA-256")
    .digest(toString().toByteArray(Charsets.UTF_8))
    .joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
