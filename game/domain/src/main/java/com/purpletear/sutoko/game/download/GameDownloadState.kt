package com.purpletear.sutoko.game.download

import androidx.annotation.Keep

/**
 * Sealed class representing the different states of a game download operation.
 */
@Keep
sealed class GameDownloadState {
    /**
     * Initial state - no download in progress.
     */
    data object Idle : GameDownloadState()

    /**
     * Download is in progress.
     * @property progress Download progress percentage (0-100)
     */
    @Keep
    data class Downloading(val progress: Int) : GameDownloadState()

    /**
     * Download completed, now extracting the archive.
     */
    data object Extracting : GameDownloadState()

    /**
     * Download and extraction completed successfully.
     */
    data object Completed : GameDownloadState()

    /**
     * An error occurred during download or extraction.
     * @property cause The exception that caused the error
     */
    @Keep
    data class Error(val cause: Throwable) : GameDownloadState()

    /**
     * Download was cancelled by the user.
     */
    data object Cancelled : GameDownloadState()
}
