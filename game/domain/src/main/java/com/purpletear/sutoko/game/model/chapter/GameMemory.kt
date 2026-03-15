package com.purpletear.sutoko.game.model.chapter

import androidx.annotation.Keep
import com.purpletear.sutoko.game.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory store for game variables with persistence support.
 * 
 * NOTE: Changes are in-memory only until save() is explicitly called.
 * This follows explicit persistence points (chapter end, pause) rather than
 * auto-save on every change for predictable behavior and simpler debugging.
 * 
 * Scoped as Singleton to maintain state across the game session,
 * but load() must be called with specific gameId to populate.
 */
@Singleton
@Keep
class GameMemory @Inject constructor(
    private val repository: MemoryRepository
) {
    private val _state = MutableStateFlow<Map<String, String>>(emptyMap())
    val state: StateFlow<Map<String, String>> = _state.asStateFlow()

    private val memory = mutableMapOf<String, String>()
    private var currentGameId: String? = null

    /**
     * Loads memories from repository for the specified game.
     * Clears any existing in-memory state first.
     */
    suspend fun load(gameId: String) {
        currentGameId = gameId
        memory.clear()
        memory.putAll(repository.load(gameId))
        _state.value = memory.toMap()
    }

    /**
     * Saves current memories to repository.
     * Must be called explicitly at save points (chapter end, pause).
     */
    suspend fun save() {
        currentGameId?.let { gameId ->
            repository.save(gameId, memory.toMap())
        }
    }

    /**
     * Clears all memories for current game (in-memory and persisted).
     */
    suspend fun clear() {
        currentGameId?.let { gameId ->
            repository.clear(gameId)
        }
        memory.clear()
        _state.value = emptyMap()
    }

    /**
     * Sets a value in memory (in-memory only until save() is called).
     */
    fun set(key: String, value: String) {
        memory[key] = value
        _state.value = memory.toMap()
    }

    fun get(key: String): String? = memory[key]

    fun getBoolean(key: String): Boolean = memory[key]?.toBoolean() ?: false

    fun getInt(key: String): Int? = memory[key]?.toIntOrNull()

    fun evaluateCondition(expression: String): Boolean {
        return ConditionEvaluator.evaluate(expression, memory)
    }

    /**
     * Returns a read-only copy of current memory state.
     */
    fun snapshot(): Map<String, String> = memory.toMap()
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
