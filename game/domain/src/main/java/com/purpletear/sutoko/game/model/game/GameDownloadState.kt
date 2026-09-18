package com.purpletear.sutoko.game.model.game

sealed interface GameDownloadState {
    data object Preparing : GameDownloadState
    data class Downloading(val progress: Float?) : GameDownloadState {
        init {
            require(progress == null || progress.isFinite() && progress in 0f..1f)
        }
    }
    data object Installing : GameDownloadState
    data class Completed(val version: Int) : GameDownloadState
    data object Failed : GameDownloadState
    data object Cancelled : GameDownloadState
}

data class GameDownloadRequest(
    val url: String,
    val version: String,
    val legacyId: Int? = null,
)
