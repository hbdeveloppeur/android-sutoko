package com.purpletear.game.presentation.game_play

internal object SmsGameRoutes {
    const val DEBUG = "debug/{gameId}"
    const val CHAPTER_SELECTION = "game/chapter-selection?currentChapterCode={currentChapterCode}"
    const val IS_TRIAL_ARG = "isTrial"
    const val AUTO_PLAY_ARG = "autoPlay"
    const val GAME = "game/play/{chapterCode}?$IS_TRIAL_ARG={$IS_TRIAL_ARG}&$AUTO_PLAY_ARG={$AUTO_PLAY_ARG}"
    const val CINEMATIC = "game/cinematic"

    fun game(
        chapterCode: String,
        isTrial: Boolean = false,
        autoPlay: Boolean = false,
    ): String =
        "game/play/$chapterCode?$IS_TRIAL_ARG=$isTrial&$AUTO_PLAY_ARG=$autoPlay"

    fun cinematic(): String = CINEMATIC

    fun debug(gameId: String): String = "debug/$gameId"

    fun chapterSelection(currentChapterCode: String): String =
        "game/chapter-selection?currentChapterCode=$currentChapterCode"
}
