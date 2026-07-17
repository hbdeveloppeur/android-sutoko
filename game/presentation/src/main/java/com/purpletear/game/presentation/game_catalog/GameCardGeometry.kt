package com.purpletear.game.presentation.game_catalog

/**
 * Geometry of the GameCard banner image (2767 x 1139 px).
 *
 * The game title is baked into the image inside the rectangle (125,129)-(1308,747).
 * Everything is expressed as fractions of the image so the layout is identical
 * at any screen size. These are the ONLY source of these numbers.
 * See docs/feature-gamecard-visuals.md.
 */
internal const val GAME_CARD_ASPECT = 2767f / 1139f

/** Horizontal center of the title rectangle: (125 + 1308) / 2 = 716.5 px. */
internal const val TITLE_CENTER_X_FRACTION = 716.5f / 2767f

/** Bottom edge of the title rectangle: 747 px. */
internal const val TITLE_BOTTOM_Y_FRACTION = 747f / 1139f

/** Contract: at most 3 short theme labels under the title. */
internal const val MAX_THEMES = 3

/**
 * Contract: each theme label is capped so long compound words (e.g. German)
 * can never push the subtitle outside the card.
 */
internal const val MAX_THEME_LENGTH = 24
