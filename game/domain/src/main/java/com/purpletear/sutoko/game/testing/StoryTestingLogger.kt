package com.purpletear.sutoko.game.testing

import com.purpletear.sutoko.game.BuildConfig

/**
 * Debug-only logger for the real-time story testing feature.
 *
 * Mirrors [com.purpletear.sutoko.game.engine.GameEngineLogger]: writes to standard
 * output so traces are visible in the terminal, Android Studio logcat, and JVM unit
 * tests without Android framework mocks. All methods are no-ops in release builds via
 * [BuildConfig.DEBUG].
 *
 * Categories are color-coded so the test flow is easy to scan:
 * - [SESS] cyan    — session lifecycle (start/stop/join)
 * - [SYNC] blue    — seed sync and asset inventory
 * - [NET]  magenta — SSE / network events
 * - [PKG]  gray    — package download and extraction
 * - [ASST] green   — asset cache operations
 * - [GRPH] yellow  — graph loading
 * - [NAV]  bright cyan   — play-from-node navigation
 * - [MEM]  bright blue   — memory namespace operations
 * - [ERR]  red           — errors and exceptions
 */
object StoryTestingLogger {

    private const val CATEGORY_WIDTH = 4
    private const val RESET = "\u001B[0m"
    private const val YELLOW = "\u001B[33m"
    private const val RED = "\u001B[31m"

    private val CATEGORY_COLORS = mapOf(
        "SESS" to "\u001B[36m", // cyan
        "SYNC" to "\u001B[34m", // blue
        "NET" to "\u001B[35m", // magenta
        "PKG" to "\u001B[90m", // gray
        "ASST" to "\u001B[32m", // green
        "GRPH" to "\u001B[33m", // yellow
        "NAV" to "\u001B[96m", // bright cyan
        "MEM" to "\u001B[94m", // bright blue
        "ERR" to "\u001B[31m", // red
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
        forcedColor: String? = null,
    ): String {
        val color = forcedColor ?: CATEGORY_COLORS[category.uppercase()] ?: ""
        val paddedCategory = category.uppercase().padEnd(CATEGORY_WIDTH)
        return "StoryTesting $color[$paddedCategory]$RESET $message"
    }
}
