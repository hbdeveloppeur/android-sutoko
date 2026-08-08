package com.purpletear.game.debug

object SmsGameDebugNodeJumps {

    private val chapterCodeToNodeId: Map<String, String> = mapOf(
        // "2a" to "Lv7FODHF5Hk-2A-105",
        "1a" to "message-1785881488476",
    )

    fun getNodeId(chapterCode: String): String? =
        chapterCodeToNodeId[chapterCode.lowercase()]
}
