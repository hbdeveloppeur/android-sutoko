package com.purpletear.game.presentation.model

import com.purpletear.sutoko.game.model.game.NarrativeTheme

/**
 * Joins server-localized narrative theme names into the display label shown on game cards and
 * previews, falling back to [fallback] when the server did not provide any themes.
 */
internal fun formatNarrativeThemes(themes: List<NarrativeTheme>, fallback: String): String =
    themes.joinToString(separator = " • ") { it.name }.ifEmpty { fallback }
