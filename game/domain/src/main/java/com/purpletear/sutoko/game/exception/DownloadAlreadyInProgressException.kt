package com.purpletear.sutoko.game.exception

/**
 * Signals that a download for the same game is already running.
 * Benign by contract: callers must treat it as a no-op (the ongoing download
 * keeps progressing), not as a user-facing failure.
 */
class DownloadAlreadyInProgressException(
    message: String = "Download already in progress",
    cause: Throwable? = null
) : Exception(message, cause)
