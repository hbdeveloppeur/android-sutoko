package com.purpletear.game.presentation.game_preview

import com.purpletear.game.presentation.BuildConfig

/**
 * Debug-only logger for the GamePreview story-loading flow.
 *
 * Writes to standard output so traces are visible in the terminal, Android
 * Studio logcat, and JVM unit tests without requiring Android framework mocks.
 * All methods are no-ops in release builds via [BuildConfig.DEBUG].
 *
 * Categories are color-coded so the flow is easy to scan:
 * - [LIFE] cyan    — ViewModel lifecycle (start/refresh/Cleared)
 * - [SYNC] blue    — game sync / network
 * - [CHAP] magenta — chapters load
 * - [OBS]  gray    — state observation (game / currentChapter emissions)
 * - [DOWN] green   — download
 * - [PUR]  yellow  — purchase
 * - [NAV]  bright cyan   — navigation to play
 * - [ERR]  red           — errors and exceptions
 */
internal object GamePreviewLogger {

    private const val CATEGORY_WIDTH = 4
    private const val RESET = "\u001B[0m"
    private const val YELLOW = "\u001B[33m"
    private const val RED = "\u001B[31m"

    private val CATEGORY_COLORS = mapOf(
        "LIFE" to "\u001B[36m", // cyan
        "SYNC" to "\u001B[34m", // blue
        "CHAP" to "\u001B[35m", // magenta
        "OBS" to "\u001B[90m", // gray
        "DOWN" to "\u001B[32m", // green
        "PUR" to "\u001B[33m", // yellow
        "NAV" to "\u001B[96m", // bright cyan
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
        return "SutokoGamePreview $color[$paddedCategory]$RESET $message"
    }
}
