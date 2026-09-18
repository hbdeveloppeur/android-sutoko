package com.purpletear.game.presentation.game_play.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/** Results are delivered once, after the fullscreen ad has closed. */
interface ChapterAdGateway {
    val availability: StateFlow<ChapterAdAvailability>
    fun preload()
    fun show(activity: Activity, onResult: (ChapterAdResult) -> Unit)
}

enum class ChapterAdResult { REWARDED, DISMISSED, UNAVAILABLE }

enum class ChapterAdAvailability { UNAVAILABLE, LOADING, READY, SHOWING }
