package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.game.repository.FriendzonedProgressRepository

class FakeFriendzonedProgressRepository : FriendzonedProgressRepository {
    var chapterCode: String = "1a"
    val resetLegacyIds = mutableListOf<Int>()
    val firstNames = mutableMapOf<Int, String>()

    override suspend fun getChapterCode(legacyId: Int): String = chapterCode

    override suspend fun reset(legacyId: Int) {
        resetLegacyIds.add(legacyId)
    }

    override suspend fun setFirstName(legacyId: Int, name: String) {
        firstNames[legacyId] = name
    }
}
