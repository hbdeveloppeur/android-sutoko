package com.purpletear.game.data.provider

import java.util.Locale

/**
 * Resolves a requested BCP-47 language tag (e.g. "fr-ES") against the chapter
 * languages actually available on disk for a game (e.g. "fr-FR", "es-ES").
 *
 * Resolution order:
 * 1. Exact match (case-insensitive).
 * 2. Same primary language, preferring its canonical directory (fr-FR, es-ES, de-DE, en-US).
 * 3. Global fallback (en-US first), then the first available language alphabetically.
 */
internal object ChapterLanguageResolver {

    private val canonicalByLanguage = mapOf(
        "fr" to "fr-FR",
        "es" to "es-ES",
        "de" to "de-DE",
        "en" to "en-GB",
    )

    private val globalFallbacks = listOf("en-US", "en-GB", "fr-FR", "es-ES", "es-419", "de-DE")

    fun resolve(requested: String, available: List<String>): String? {
        if (available.isEmpty()) return null

        fun find(tag: String): String? = available.firstOrNull { it.equals(tag, ignoreCase = true) }

        find(requested)?.let { return it }

        val language = requested.substringBefore('-').lowercase(Locale.ROOT)
        val sameLanguage = available.filter {
            it.substringBefore('-').lowercase(Locale.ROOT) == language
        }
        if (sameLanguage.isNotEmpty()) {
            val canonical = canonicalByLanguage[language]
            return sameLanguage.firstOrNull { it.equals(canonical, ignoreCase = true) }
                ?: sameLanguage.minByOrNull { it.lowercase(Locale.ROOT) }
        }

        return globalFallbacks.firstNotNullOfOrNull { find(it) }
            ?: available.minByOrNull { it.lowercase(Locale.ROOT) }
    }
}
