package com.purpletear.game.debug.ruler

import androidx.compose.ui.graphics.Color
import java.util.UUID
import androidx.annotation.Keep

/**
 * Orientation of a ruler line.
 */
enum class RulerOrientation {
    HORIZONTAL,
    VERTICAL
}

/**
 * Immutable state of a single ruler.
 *
 * @property id Unique identifier for this ruler
 * @property orientation Whether the ruler is horizontal or vertical
 * @property position Position in percentage of container (0f..1f)
 * @property color Visual color of the ruler line
 */
@Keep
data class Ruler(
    val id: String,
    val orientation: RulerOrientation,
    val position: Float,
    val color: Color = Color.Cyan
) {
    init {
        require(position in 0f..1f) { "Position must be in range [0, 1], was $position" }
    }

    companion object {
        fun create(
            orientation: RulerOrientation,
            position: Float,
            color: Color = Color.Cyan
        ): Ruler = Ruler(
            id = UUID.randomUUID().toString(),
            orientation = orientation,
            position = position.coerceIn(0f, 1f),
            color = color
        )

        /**
         * Creates a Ruler from saved string parts with error handling.
         * Returns null if parsing fails.
         */
        fun fromParts(parts: List<String>): Ruler? {
            if (parts.size < 3) return null
            return try {
                Ruler(
                    id = parts[0],
                    orientation = RulerOrientation.valueOf(parts[1]),
                    position = parts[2].toFloat().coerceIn(0f, 1f),
                    color = if (parts.size >= 4) {
                        parseColor(parts[3])
                    } else Color.Cyan
                )
            } catch (_: Exception) {
                null
            }
        }

        private fun parseColor(colorStr: String): Color {
            return try {
                when {
                    colorStr == "Cyan" -> Color.Cyan
                    colorStr.startsWith("0x") || colorStr.startsWith("#") -> {
                        val hex = colorStr.removePrefix("0x").removePrefix("#")
                        Color(hex.toLong(16))
                    }
                    else -> {
                        // Try parsing as ULong (Compose Color.value format)
                        Color(colorStr.toULong())
                    }
                }.let { restored ->
                    // Guard against transparent or invalid colors
                    if (restored.alpha <= 0f) Color.Cyan else restored
                }
            } catch (_: Exception) {
                Color.Cyan
            }
        }
    }
}
