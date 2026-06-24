package com.purpletear.sutoko.game.model.chapter

import androidx.annotation.Keep
import com.purpletear.sutoko.game.engine.GameEngineLogger
import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.model.chapter.GameMemory.Companion.CONVERSATION_MODE_KEY
import com.purpletear.sutoko.game.model.chapter.GameMemory.Companion.TYPING_ANIMATION_KEY
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * In-memory store for game variables with persistence support.
 *
 * NOTE: Changes are in-memory only until save() is explicitly called.
 * This follows explicit persistence points (chapter end, pause) rather than
 * auto-save on every change for predictable behavior and simpler debugging.
 *
 * One instance per game session to ensure isolation between games.
 * load() must be called with specific gameId to populate.
 */
@Keep
class GameMemory @Inject constructor(
    private val repository: MemoryRepository,
    private val progressRepository: UserGameProgressRepository,
) {
    private val _state = MutableStateFlow<Map<String, String>>(emptyMap())
    val state: StateFlow<Map<String, String>> = _state.asStateFlow()

    private val memory = mutableMapOf<String, MemoryEntry>()
    private var currentGameId: String? = null
    private var currentChapterCode: String? = null
    private var currentChapterNumber: Int? = null

    /**
     * The currently loaded game ID, or null if not loaded.
     */
    val gameId: String? get() = currentGameId

    /**
     * Loads memories from repository for the specified game, keeping only state from
     * chapters up to and including [chapterNumber]. Memories written in this chapter
     * or any later chapter are deleted first to prevent overlap when replaying.
     *
     * Clears any existing in-memory state first.
     */
    suspend fun load(gameId: String, chapterNumber: Int) {
        currentGameId = gameId
        currentChapterNumber = chapterNumber
        memory.clear()
        memory.putAll(repository.load(gameId, chapterNumber))
        _state.value = memory.mapValues { it.value.value }
        GameEngineLogger.d("MEM") { "Loaded for gameId=$gameId up to chapter $chapterNumber — ${memory.size} entries" }
    }

    /**
     * Saves current memories to repository.
     * Must be called explicitly at save points (chapter end, pause).
     */
    suspend fun save() {
        currentGameId?.let { gameId ->
            repository.save(gameId, memory.toMap())
            GameEngineLogger.d("MEM") { "Saved for gameId=$gameId — ${memory.size} entries" }
            currentChapterCode?.let { chapter ->
                progressRepository.save(
                    UserGameProgress(
                        gameId = gameId,
                        currentChapterCode = chapter,
                        normalizedChapterCode = chapter.lowercase()
                    )
                )
            }
        }
    }

    /**
     * Sets the current chapter code.
     * This is persisted when save() is called.
     */
    fun setCurrentChapter(chapterCode: String) {
        currentChapterCode = chapterCode
    }

    /**
     * Sets the current chapter number.
     * New memories written via [set] are tagged with this number.
     */
    fun setCurrentChapterNumber(chapterNumber: Int) {
        currentChapterNumber = chapterNumber
    }

    /**
     * Clears all memories for current game (in-memory and persisted).
     */
    suspend fun clear() {
        currentGameId?.let { gameId ->
            repository.clear(gameId)
        }
        memory.clear()
        currentChapterCode = null
        currentChapterNumber = null
        _state.value = emptyMap()
        GameEngineLogger.d("MEM") { "Cleared" }
    }

    /**
     * Sets a value in memory (in-memory only until save() is called).
     * The value is tagged with the current chapter number.
     *
     * @throws IllegalStateException if no chapter number has been set.
     */
    fun set(key: String, value: String) {
        val chapterNumber = checkNotNull(currentChapterNumber) {
            "Current chapter number must be set before setting memory '$key'"
        }
        memory[key] = MemoryEntry(value, chapterNumber)
        _state.value = memory.mapValues { it.value.value }
        GameEngineLogger.d("MEM") { "Set $key=$value (chapter $chapterNumber)" }
    }

    fun get(key: String): String? = memory[key]?.value

    /**
     * Current conversation mode for message display.
     *
     * Priority:
     * 1. [TYPING_ANIMATION_KEY] memory value:
     *    - "true"  -> SMS mode (typing indicators + delays)
     *    - "false" -> IRL mode (immediate display)
     * 2. [CONVERSATION_MODE_KEY] memory value (legacy):
     *    - "SMS" -> SMS mode
     *    - "IRL" -> IRL mode
     * 3. Default -> IRL mode
     */
    val conversationMode: ConversationMode
        get() {
            when (get(TYPING_ANIMATION_KEY)?.lowercase()) {
                "true" -> return ConversationMode.SMS
                "false" -> return ConversationMode.IRL
            }

            val modeString = get(CONVERSATION_MODE_KEY)
            return try {
                modeString?.let { ConversationMode.valueOf(it) } ?: ConversationMode.IRL
            } catch (_: IllegalArgumentException) {
                ConversationMode.IRL
            }
        }

    fun evaluateCondition(expression: String): Boolean {
        val result = ConditionEvaluator.evaluate(expression, memory.mapValues { it.value.value })
        GameEngineLogger.d("COND") { "\"$expression\" = $result" }
        return result
    }

    /**
     * Returns a read-only copy of current memory state.
     */
    fun snapshot(): Map<String, String> = memory.mapValues { it.value.value }

    companion object {
        /**
         * Memory key that overrides conversation mode when present.
         * - "true"  -> SMS mode
         * - "false" -> IRL mode
         */
        const val TYPING_ANIMATION_KEY = "typingAnimation"

        /**
         * Legacy memory key for storing current conversation mode.
         * Used by [ConversationModeChangeNodeHandler] and read by [MessageNodeHandler].
         */
        const val CONVERSATION_MODE_KEY = "conversation_mode"
    }
}

@Keep
internal object ConditionEvaluator {
    fun evaluate(expression: String, memory: Map<String, String>): Boolean {
        val cleanExpr = expression.trim()

        if (cleanExpr.contains("&&") || cleanExpr.contains("||")) {
            return evaluateComplex(cleanExpr, memory)
        }

        return evaluateSimple(cleanExpr, memory)
    }

    private fun evaluateSimple(expression: String, memory: Map<String, String>): Boolean {
        val operators = listOf("==", "!=", ">=", "<=", ">", "<")

        for (op in operators) {
            val parts = expression.split(op)
            if (parts.size == 2) {
                val left = parts[0].trim()
                val right = parts[1].trim()
                val leftValue = memory[left] ?: left

                return when (op) {
                    "==" -> leftValue == right
                    "!=" -> leftValue != right
                    ">=" -> (leftValue.toFloatOrNull() ?: 0f) >= (right.toFloatOrNull() ?: 0f)
                    "<=" -> (leftValue.toFloatOrNull() ?: 0f) <= (right.toFloatOrNull() ?: 0f)
                    ">" -> (leftValue.toFloatOrNull() ?: 0f) > (right.toFloatOrNull() ?: 0f)
                    "<" -> (leftValue.toFloatOrNull() ?: 0f) < (right.toFloatOrNull() ?: 0f)
                    else -> false
                }
            }
        }

        return memory[expression]?.toBoolean() ?: false
    }

    private fun evaluateComplex(expression: String, memory: Map<String, String>): Boolean {
        val orParts = expression.split("||")

        return orParts.any { orPart ->
            val andParts = orPart.split("&&")
            andParts.all { andPart ->
                evaluateSimple(andPart.trim(), memory)
            }
        }
    }
}
