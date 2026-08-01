package com.purpletear.sutoko.game.repository

/**
 * Progress of Friendzoned games (see [com.purpletear.sutoko.game.model.FriendzonedLegacyIds]).
 * Those games persist their chapter progress themselves in `TableOfSymbols`
 * (:tools); this repository is the `:game`-side read/reset gateway to it.
 */
interface FriendzonedProgressRepository {

    /**
     * Current chapter code as stored by the game (lowercase, e.g. "7a").
     * Returns "1a" when the game has no stored progress yet.
     */
    suspend fun getChapterCode(legacyId: Int): String

    /**
     * Resets the game's progress to the beginning. Mirrors the in-game reset
     * semantics: the story version and escape-game flag are preserved.
     */
    suspend fun reset(legacyId: Int)

    /**
     * Persists the player's first name so the Friendzoned games can substitute
     * `[prenom]` in their phrases and contact names. [name] must already be
     * sanitized (see `UserNickNameSanitizer`).
     */
    suspend fun setFirstName(legacyId: Int, name: String)
}
