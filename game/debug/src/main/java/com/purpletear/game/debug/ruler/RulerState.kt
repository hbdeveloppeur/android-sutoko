package com.purpletear.game.debug.ruler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color

/**
 * State holder for managing ruler lines.
 *
 * Contract:
 * - All positions are stored as Float in range [0f, 1f] representing percentage of container
 * - All mutations are performed through explicit methods that validate invariants
 * - State is observable via Compose's SnapshotStateList
 */
@Stable
class RulerState(
    initialRulers: List<Ruler> = emptyList()
) {
    private val _rulers: SnapshotStateList<Ruler> = mutableStateListOf<Ruler>().apply {
        addAll(initialRulers)
    }

    /**
     * Read-only view of current rulers.
     */
    val rulers: List<Ruler> get() = _rulers

    /**
     * Returns a snapshot of current rulers for saving.
     */
    fun toList(): List<Ruler> = _rulers.toList()

    /**
     * Adds a new ruler at the specified position.
     *
     * @param orientation Horizontal or vertical ruler
     * @param position Position in percentage (0f..1f), will be coerced to valid range
     * @param color Visual color of the ruler
     * @return The created ruler's id
     */
    fun add(
        orientation: RulerOrientation,
        position: Float,
        color: Color = Color.Cyan
    ): String {
        val ruler = Ruler.create(orientation, position, color)
        _rulers.add(ruler)
        return ruler.id
    }

    /**
     * Moves an existing ruler to a new position.
     *
     * Precondition: Ruler with [id] must exist
     * Postcondition: Position is coerced to [0f, 1f]
     *
     * @param id The ruler's unique identifier
     * @param newPosition New position in percentage (0f..1f)
     * @return true if ruler was found and updated, false otherwise
     */
    fun move(id: String, newPosition: Float): Boolean {
        val index = _rulers.indexOfFirst { it.id == id }
        if (index == -1) return false

        val ruler = _rulers[index]
        _rulers[index] = ruler.copy(position = newPosition.coerceIn(0f, 1f))
        return true
    }

    /**
     * Deletes a ruler by id.
     *
     * @param id The ruler's unique identifier
     * @return true if ruler was found and removed, false otherwise
     */
    fun delete(id: String): Boolean {
        return _rulers.removeAll { it.id == id }
    }

    /**
     * Removes all rulers.
     */
    fun clear() {
        _rulers.clear()
    }

    /**
     * Returns the number of rulers.
     */
    val count: Int get() = _rulers.size

    /**
     * Returns true if there are no rulers.
     */
    val isEmpty: Boolean get() = _rulers.isEmpty()

    companion object {
        /**
         * Saver for persisting RulerState across configuration changes and Live Edit.
         * Format: id|orientation|position|color,id|orientation|position|color,...
         */
        val Saver: Saver<RulerState, String> = Saver(
            save = { state ->
                state.rulers.joinToString(",") { ruler ->
                    // Use simple "Cyan" string for default color, or hex for custom
                    val colorStr = when (ruler.color) {
                        Color.Cyan -> "Cyan"
                        else -> "0x${ruler.color.value.toString(16)}"
                    }
                    "${ruler.id}|${ruler.orientation.name}|${ruler.position}|$colorStr"
                }
            },
            restore = { data ->
                if (data.isEmpty()) {
                    RulerState(emptyList())
                } else {
                    val rulers = data.split(",").mapNotNull { rulerStr ->
                        Ruler.fromParts(rulerStr.split("|"))
                    }
                    RulerState(rulers)
                }
            }
        )
    }
}

/**
 * Creates and remembers a [RulerState] instance that survives configuration changes and Live Edit.
 */
@Composable
fun rememberRulerState(): RulerState {
    return rememberSaveable(saver = RulerState.Saver) { RulerState() }
}
