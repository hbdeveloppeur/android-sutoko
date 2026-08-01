package com.purpletear.game.data.repository

import com.purpletear.sutoko.game.repository.FriendzonedProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import purpletear.fr.purpleteartools.TableOfSymbols
import javax.inject.Inject

/**
 * [FriendzonedProgressRepository] backed by [TableOfSymbols] (:tools), the
 * store the Friendzoned games themselves read on launch and write on chapter
 * transitions. Storage is wired by the app module; when absent (unit tests)
 * reads degrade to the default chapter and writes are no-ops.
 */
class FriendzonedProgressRepositoryImpl @Inject constructor() : FriendzonedProgressRepository {

    override suspend fun getChapterCode(legacyId: Int): String = withContext(Dispatchers.IO) {
        val symbols = TableOfSymbols(legacyId)
        symbols.read()
        // TableOfSymbols defaults to "1a" when no progress was ever stored.
        symbols.chapterCode
    }

    override suspend fun reset(legacyId: Int) = withContext(Dispatchers.IO) {
        val symbols = TableOfSymbols(legacyId)
        symbols.read()
        // Same reset the games use themselves: keeps story version and the
        // escape-game flag, marks the story as replayed.
        symbols.reset(legacyId)
        symbols.save()
        Unit
    }

    override suspend fun setFirstName(legacyId: Int, name: String) = withContext(Dispatchers.IO) {
        val symbols = TableOfSymbols(legacyId)
        symbols.read()
        // Row 0 feeds [prenom] phrase substitution in all four games; the
        // per-game row feeds contact/conversation names (fz1, fz4).
        symbols.globalFirstName = name
        symbols.firstName = name
        symbols.save()
        Unit
    }
}
