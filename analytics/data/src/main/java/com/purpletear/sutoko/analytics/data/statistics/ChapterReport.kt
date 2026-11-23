package com.purpletear.sutoko.analytics.data.statistics

import java.util.Locale

data class ChapterKey(val engine: String, val gameId: String, val language: String, val chapter: String) {
    companion object {
        fun validated(engine: String, gameId: String, language: String, chapter: String): ChapterKey? {
            val lang = language.lowercase(Locale.ROOT)
            val code = chapter.lowercase(Locale.ROOT)
            if (engine !in setOf("sms", "friendzoned") || !gameId.matches(Regex("[A-Za-z0-9_-]{1,64}")) ||
                !lang.matches(Regex("[a-z]{2,3}(-[a-z0-9]{2,8}){0,3}")) || lang.length > 35 ||
                (code.isNotEmpty() && !code.matches(Regex("[a-z0-9][a-z0-9_-]{0,31}")))) return null
            if (engine == "friendzoned" && gameId !in setOf("162", "161", "159", "163")) return null
            return ChapterKey(engine, gameId, lang, code)
        }
    }
}

data class ChapterReport(
    val eventId: String,
    val runId: String,
    val key: ChapterKey,
    val kind: String,
    val at: Long,
    val replay: Boolean,
    val visitorId: String = runId,
    val previewId: String? = null,
    val mode: String = "full",
    val reason: String = "",
)
