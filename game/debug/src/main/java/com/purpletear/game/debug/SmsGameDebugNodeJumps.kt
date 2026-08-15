package com.purpletear.game.debug

object SmsGameDebugNodeJumps {

    private val chapterCodeToNodeId: Map<String, String> = mapOf(
        // "2a" to "Lv7FODHF5Hk-2A-105",
        // "3b" to "yPtw4MrFlWn-3B-97",
    )

    fun getNodeId(chapterCode: String): String? =
        chapterCodeToNodeId[chapterCode.lowercase()]
}
