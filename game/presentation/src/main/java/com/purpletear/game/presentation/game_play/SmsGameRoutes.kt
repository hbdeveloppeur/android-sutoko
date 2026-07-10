package com.purpletear.game.presentation.game_play

internal object SmsGameRoutes {
    const val DEBUG = "debug/{gameId}"
    const val CHAPTER_SELECTION = "game/chapter-selection?currentChapterCode={currentChapterCode}"
    const val IS_LIVE_UPDATE_MODE_ARG = "isLiveUpdateMode"
    const val GAME = "game/play/{chapterCode}?$IS_LIVE_UPDATE_MODE_ARG={$IS_LIVE_UPDATE_MODE_ARG}"
    const val CINEMATIC = "game/cinematic"

    fun game(chapterCode: String, isLiveUpdateMode: Boolean = false): String =
        "game/play/$chapterCode?$IS_LIVE_UPDATE_MODE_ARG=$isLiveUpdateMode"

    fun cinematic(): String = CINEMATIC

    fun debug(gameId: String): String = "debug/$gameId"

    fun chapterSelection(currentChapterCode: String): String =
        "game/chapter-selection?currentChapterCode=$currentChapterCode"
}
