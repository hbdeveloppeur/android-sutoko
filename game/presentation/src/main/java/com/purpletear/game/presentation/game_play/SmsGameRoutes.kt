package com.purpletear.game.presentation.game_play

internal object SmsGameRoutes {
    const val DEBUG = "debug/{gameId}"
    const val DESCRIPTION = "game/description"
    const val GAME = "game/play/{chapterCode}"

    fun game(chapterCode: String): String = "game/play/$chapterCode"

    fun debug(gameId: String): String = "debug/$gameId"
}