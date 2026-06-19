package com.purpletear.sutoko.game.model.game

import androidx.annotation.Keep

@Keep
data class GameInstall(
    val gameId: String,
    val localVersion: String? = null,
)
