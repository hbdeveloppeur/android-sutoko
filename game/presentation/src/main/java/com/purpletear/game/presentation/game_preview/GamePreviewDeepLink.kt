package com.purpletear.game.presentation.game_preview

/**
 * Single source of truth for the public "open this game" HTTPS link.
 *
 * The link is shared with friends from the GamePreview screen and routed back
 * into the app by :app (AndroidManifest intent-filter + nav deep link on the
 * GamePreview destination).
 */
object GamePreviewDeepLink {
    const val URI_PATTERN: String = "https://sutoko.com/game/{gameId}"

    fun url(gameId: String): String = "https://sutoko.com/game/$gameId"
}
