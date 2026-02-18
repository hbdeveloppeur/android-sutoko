package com.purpletear.sutoko.game.exception

/**
 * Exception thrown when a game download is forbidden (HTTP 403).
 * This typically occurs when the user doesn't have permission to download the game.
 *
 * @param message The error message
 * @param cause The cause of the exception
 */
class GameDownloadForbiddenException(
    message: String = "Game download forbidden. You don't have permission to download this game.",
    cause: Throwable? = null
) : Exception(message, cause)