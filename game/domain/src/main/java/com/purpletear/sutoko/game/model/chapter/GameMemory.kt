package com.purpletear.sutoko.game.model.chapter

import androidx.annotation.Keep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Keep
class GameMemory {
    private val _state = MutableStateFlow<Map<String, String>>(emptyMap())
    val state: StateFlow<Map<String, String>> = _state.asStateFlow()

    private val memory = mutableMapOf<String, String>()

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

    fun clear() {
        memory.clear()
        _state.value = emptyMap()
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
