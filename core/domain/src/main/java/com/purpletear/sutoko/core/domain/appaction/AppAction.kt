package com.purpletear.sutoko.core.domain.appaction

import androidx.annotation.Keep

/**
 * Represents an action attached to a News item or other app features.
 *
 * The name of the action is limited to the values defined in [ActionName].
 * The optional [value] carries additional data (e.g., URL, page identifier, etc.).
 */
@Keep
data class AppAction(
    val name: ActionName,
    val value: String?,
)

/**
 * Allowed action names for App actions.
 */
@Keep
enum class ActionName {
    OpenLink,
    OpenGame,
    OpenPage;

    companion object {

        /**
         * Resolves the given raw string into an [ActionName] enumeration value or throws an exception
         * if the input is null or does not match any known action names.
         *
         * @param raw The raw string representing an action name. It may be in any case format.
         * @return The corresponding [ActionName] if the input matches a known action name.
         * @throws InvalidActionNameException If the input is null or does not match any valid action name.
         */
        fun fromStringOrThrow(raw: String?): ActionName = when (raw) {
            null -> throw InvalidActionNameException("AppAction name is missing")
            "OpenLink" -> OpenLink
            "OpenGame" -> OpenGame
            "OpenPage" -> OpenPage
            else -> throw InvalidActionNameException("Invalid action name: $raw")
        }
    }
}
