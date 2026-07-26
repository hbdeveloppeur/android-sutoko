package com.purpletear.sutoko.game.model

/**
 * Legacy integer IDs of the Friendzoned games that manage their own chapter
 * progress in `TableOfSymbols` (:tools) and run in their dedicated
 * `:games:friendzone*` activities (see app `FriendzonedGameRouter`).
 *
 * Distinct from the buy-only legacy set in `GameActionState` (presentation),
 * which also contains SMS (160): SMS runs on the standard `:game` engine, so
 * its progress stays in the Room user-progress store. Keep in sync with
 * `FriendzonedGameRouter`.
 */
object FriendzonedLegacyIds {
    private val ids = setOf(159, 161, 162, 163)

    fun isFriendzoned(legacyId: Int?): Boolean = legacyId != null && legacyId in ids
}
