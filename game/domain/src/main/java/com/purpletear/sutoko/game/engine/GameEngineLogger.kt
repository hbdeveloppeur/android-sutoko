package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.BuildConfig

/**
 * Debug-only logger for the game engine.
 *
 * Writes to standard output so traces are visible in the terminal, Android
 * Studio logcat, and JVM unit tests without requiring Android framework mocks.
 * All methods are no-ops in release builds via [BuildConfig.DEBUG].
 *
 * Categories are color-coded so the flow is easy to scan:
 * - [GAME] cyan    — engine lifecycle
 * - [NODE] blue    — node execution
 * - [HAND] magenta — handler results
 * - [CMD]  gray    — emitted commands (Emit/Delay/AwaitInput)
 * - [FX]   green   — applied effects
 * - [NAV]  yellow  — navigation / resolver
 * - [COND] bright cyan   — condition evaluation
 * - [MEM]  bright blue   — memory operations
 * - [INPT] bright magenta — input / choices
 *
 * Warning and error lines keep their category but are tinted yellow/red.
 *
 * Example output:
 * ```
 * SutokoGameEngine [GAME] Initialized — gameId=xyz, chapter=ch_01
 * SutokoGameEngine [NODE] Executing msg_42 (Message) in ch_01
 * SutokoGameEngine [HAND] MessageNodeHandler → 5 commands, nextNodeId=null
 * SutokoGameEngine [FX]   Add MessageText from character 1: "Hello there"
 * SutokoGameEngine [NAV]  → msg_43 (edge from msg_42)
 * SutokoGameEngine [COND] "score >= 100" = false → fallback_1
 * SutokoGameEngine [INPT] Pausing for choice at choice_1 (3 options)
 * ```
 */
internal object GameEngineLogger {

    private const val CATEGORY_WIDTH = 4
    private const val RESET = "\u001B[0m"
    private const val YELLOW = "\u001B[33m"
    private const val RED = "\u001B[31m"

    private val CATEGORY_COLORS = mapOf(
        "GAME" to "\u001B[36m", // cyan
        "NODE" to "\u001B[34m", // blue
        "HAND" to "\u001B[35m", // magenta
        "CMD" to "\u001B[90m", // gray
        "FX" to "\u001B[32m", // green
        "NAV" to "\u001B[33m", // yellow
        "COND" to "\u001B[96m", // bright cyan
        "MEM" to "\u001B[94m", // bright blue
        "INPT" to "\u001B[95m", // bright magenta
    )

    fun d(category: String, message: () -> String) {
        if (BuildConfig.DEBUG) {
            println(formatLine(category, message()))
        }
    }

    fun i(category: String, message: () -> String) {
        if (BuildConfig.DEBUG) {
            println(formatLine(category, message()))
        }
    }

    fun w(category: String, message: () -> String) {
        if (BuildConfig.DEBUG) {
            println(formatLine(category, message(), forcedColor = YELLOW))
        }
    }

    fun e(category: String = "ERR", throwable: Throwable? = null, message: () -> String) {
        if (BuildConfig.DEBUG) {
            val suffix = throwable?.let { "\n${it.stackTraceToString()}" } ?: ""
            println(formatLine(category, "${message()}$suffix", forcedColor = RED))
        }
    }

    private fun formatLine(
        category: String,
        message: String,
        forcedColor: String? = null
    ): String {
        val color = forcedColor ?: CATEGORY_COLORS[category.uppercase()] ?: ""
        val paddedCategory = category.uppercase().padEnd(CATEGORY_WIDTH)
        // Keep a plain-text "SutokoGameEngine" prefix so logcat/terminal filtering is trivial.
        return "SutokoGameEngine $color[$paddedCategory]$RESET $message"
    }
}
