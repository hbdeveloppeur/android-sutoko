package com.purpletear.sutoko.game.engine.message

import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType

/**
 * Engine message for a manga page. Carries the resolved [imageUrl] and the [overlays]
 * (already substituted, e.g. `[prenom]` -> hero name) to be drawn on top of the page
 * by the presentation layer.
 *
 * [TextOverlay] is the UI-facing overlay model; the authoring/parsed equivalent is
 * `Node.MangaPage.MangaMessage`.
 */
class GameMessageMangaPage(
    id: String,
    val imageUrl: String,
    val overlays: List<TextOverlay>,
) : GameMessage(
    id = id,
    type = GameMessageType.MangaPage,
) {
    data class TextOverlay(
        val text: String,
        val size: Float,
        val x: Float,
        val y: Float,
        val w: Float,
    )
}
