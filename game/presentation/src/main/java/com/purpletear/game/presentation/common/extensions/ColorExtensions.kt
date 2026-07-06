package com.purpletear.game.presentation.common.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.core.graphics.toColorInt
import com.purpletear.sutoko.game.model.character.CharacterColor

/**
 * Parses a hex color string into a Compose Color.
 * Supports formats: #RRGGBB, #AARRGGBB, RRGGBB, ARRGGBB
 *
 * @param colorCode The hex color string to parse
 * @return The parsed Color, or Black if parsing fails
 */
fun Color.Companion.parse(colorCode: String): Color {
    val trimmed = colorCode.trim()

    val hex = when {
        trimmed.startsWith("#") -> trimmed.substring(1)
        else -> trimmed
    }

    return try {
        val colorInt = when (hex.length) {
            6 -> "#$hex".toColorInt()
            8 -> {
                val alpha = hex.substring(0, 2).toInt(16)
                val rgb = hex.substring(2).toInt(16)
                (alpha shl 24) or (rgb and 0x00FFFFFF)
            }

            else -> return Color.Black
        }
        Color(colorInt)
    } catch (_: Exception) {
        Color.Black
    }
}

/**
 * Returns the [endingColor] as a Compose [Color].
 * Invalid hex strings are parsed to [Color.Black] by [Color.parse].
 */
fun CharacterColor.toComposeColor(): Color = Color.parse(endingColor)

/**
 * Returns the [endingColor] as a Compose [Color], blended toward [Color.White]
 * by [fraction]. 0.0 returns the original color; 1.0 returns white.
 */
fun CharacterColor.toWhitenedComposeColor(fraction: Float = 0.5f): Color =
    lerp(toComposeColor(), Color.White, fraction.coerceIn(0f, 1f))
