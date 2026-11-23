package com.purpletear.sutoko.game.model.chapter

import androidx.annotation.Keep
import com.purpletear.sutoko.game.engine.GameEngineLogger
import com.purpletear.sutoko.game.engine.GameCheckpoint
import com.purpletear.sutoko.game.repository.GameProgressTransaction
import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.model.chapter.GameMemory.Companion.CONVERSATION_MODE_KEY
import com.purpletear.sutoko.game.model.chapter.GameMemory.Companion.TYPING_ANIMATION_KEY
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * In-memory store for game variables with persistence support.
 *
 * NOTE: Changes are in-memory only until save() is explicitly called.
 * The engine commits snapshots at stable reading/choice gates and chapter transitions,
 * never halfway through a script.
 *
 * Scoped to the reader ViewModel so its engine and resolver share memory without
 * another story or trial replacing the session beneath them.
 */
@Keep
@ViewModelScoped
class GameMemory @Inject constructor(
    private val repository: MemoryRepository,
    private val progressRepository: UserGameProgressRepository,
    private val transaction: GameProgressTransaction = GameProgressTransaction { it() },
) {
    private val _state = MutableStateFlow<Map<String, String>>(emptyMap())
    val state: StateFlow<Map<String, String>> = _state.asStateFlow()

    private val memory = mutableMapOf<String, MemoryEntry>()
    private var currentGameId: String? = null
    private var currentChapterCode: String? = null
    private var currentChapterNumber: Int? = null
    private var mainCharacterId: Int? = null

    /**
     * The currently loaded game ID, or null if not loaded.
     */
    val gameId: String? get() = currentGameId

    /**
     * Loads memories from repository for the specified game, keeping only state from
     * chapters before [chapterNumber]. Memories written in this chapter
     * or any later chapter are deleted first to prevent overlap when replaying.
     *
     * Clears any existing in-memory state first.
     */
    suspend fun load(gameId: String, chapterNumber: Int) {
        var loaded: Map<String, MemoryEntry> = emptyMap()
        transaction.run {
            loaded = repository.load(gameId, chapterNumber)
            val progress = progressRepository.get(gameId)
            if (progress.checkpoint != null) progressRepository.save(progress.copy(checkpoint = null))
        }
        restore(gameId, chapterNumber, loaded)
        GameEngineLogger.d("MEM") { "Loaded for gameId=$gameId before chapter $chapterNumber — ${memory.size} entries" }
    }

    suspend fun getCheckpoint(gameId: String): GameCheckpoint? = progressRepository.get(gameId).checkpoint

    suspend fun restore(gameId: String, chapterNumber: Int, entries: Map<String, MemoryEntry>) {
        currentGameId = gameId
        currentChapterNumber = chapterNumber
        memory.clear()
        memory.putAll(entries)
        val heroName = progressRepository.get(gameId).heroName
        if (heroName.isNotBlank()) memory[HERO_NAME_KEY] = MemoryEntry(heroName, chapterNumber)
        _state.value = memory.mapValues { it.value.value }
    }

    fun entries(): Map<String, MemoryEntry> = memory.toMap()

    /** Memory history and its reading gate must commit together, including on cancellation. */
    suspend fun saveCheckpoint(checkpoint: GameCheckpoint) {
        val gameId = checkNotNull(currentGameId)
        check(checkpoint.chapterCode.equals(currentChapterCode, ignoreCase = true))
        transaction.run {
            repository.save(gameId, checkpoint.memories)
            val progress = progressRepository.get(gameId)
            progressRepository.save(progress.copy(
                currentChapterCode = checkpoint.chapterCode,
                normalizedChapterCode = checkpoint.chapterCode.lowercase(),
                heroName = checkpoint.memories[HERO_NAME_KEY]?.value ?: progress.heroName,
                checkpoint = checkpoint,
            ))
        }
    }

    /** Saves a chapter transition and clears the old chapter's reading gate atomically. */
    suspend fun save() {
        val gameId = currentGameId ?: return
        val snapshot = entries()
        val chapter = currentChapterCode
        transaction.run {
            repository.save(gameId, snapshot)
            if (chapter != null) {
                val progress = progressRepository.get(gameId)
                progressRepository.save(progress.copy(
                    currentChapterCode = chapter,
                    normalizedChapterCode = chapter.lowercase(),
                    heroName = snapshot[HERO_NAME_KEY]?.value ?: progress.heroName,
                    checkpoint = null,
                ))
            }
        }
        GameEngineLogger.d("MEM") { "Saved for gameId=$gameId — ${snapshot.size} entries" }
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
            transaction.run {
                repository.clear(gameId)
                val progress = progressRepository.get(gameId)
                if (progress.checkpoint != null) progressRepository.save(progress.copy(checkpoint = null))
            }
        }
        memory.clear()
        currentChapterCode = null
        currentChapterNumber = null
        mainCharacterId = null
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

    /**
     * Sets the id of the main character for the current session.
     * This is transient session state; it is not persisted.
     */
    fun setMainCharacterId(id: Int) {
        require(id > 0) { "Main character id must be positive, was $id" }
        mainCharacterId = id
        GameEngineLogger.d("MEM") { "Main character id set to $id" }
    }

    /**
     * Returns true if the given character id is the main character.
     * Always returns false if no main character id has been set.
     */
    fun isMainCharacter(characterId: Int): Boolean =
        characterId == mainCharacterId

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

        /**
         * Memory key holding the active message bubble background color (hex, e.g. "#FF2200").
         * Written by [MessageThemeNodeHandler] and read by [MessageNodeHandler] to stamp the
         * color onto each emitted [com.purpletear.sutoko.game.engine.message.GameMessageText].
         */
        const val MESSAGE_THEME_BG_KEY = "message_theme_bg"

        /**
         * Memory key holding the active message text foreground color (hex, e.g. "#FF2200").
         * Written by [MessageThemeNodeHandler] and read by [MessageNodeHandler].
         */
        const val MESSAGE_THEME_FG_KEY = "message_theme_fg"

        /**
         * Memory key used to resolve the [prenom] variable.
         */
        const val HERO_NAME_KEY = "heroName"
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
