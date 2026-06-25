package fr.purpletear.sutoko.friendzoned

import android.content.Intent
import androidx.activity.ComponentActivity
import purpletear.fr.purpleteartools.GlobalData
import fr.purpletear.friendzone.activities.load.Load as Friendzoned1Loader
import fr.purpletear.friendzone2.activities.load.Load as Friendzoned2Loader
import fr.purpletear.friendzone4.game.activities.load.Load as Friendzoned4Loader
import friendzone3.purpletear.fr.friendzon3.Load as Friendzoned3Loader

/**
 * Maps legacy Friendzoned game IDs to their dedicated :games module loaders.
 *
 * The mapping must stay exhaustive with respect to [GlobalData.Game] legacy entries;
 * an assertion guards against a future enum value being added without a route.
 */
object FriendzonedGameRouter {

    private val legacyIdToLoader: Map<Int, Class<*>> = mapOf(
        GlobalData.Game.FRIENDZONE.id to Friendzoned1Loader::class.java,
        GlobalData.Game.FRIENDZONE2.id to Friendzoned2Loader::class.java,
        GlobalData.Game.FRIENDZONE3.id to Friendzoned3Loader::class.java,
        GlobalData.Game.FRIENDZONE4.id to Friendzoned4Loader::class.java,
    )

    init {
        val expectedLegacyIds = GlobalData.Game.values()
            .filter { it != GlobalData.Game.SMS }
            .map { it.id }
            .toSortedSet()
        val mappedLegacyIds = legacyIdToLoader.keys.toSortedSet()
        check(expectedLegacyIds == mappedLegacyIds) {
            "FriendzonedGameRouter mapping is out of sync with GlobalData.Game. " +
                "Expected $expectedLegacyIds but mapped $mappedLegacyIds"
        }
    }

    /**
     * Returns the loader Activity class for the given legacy ID, or null if this
     * legacy ID does not correspond to a built-in Friendzoned game.
     */
    fun loaderClassFor(legacyId: Int): Class<*>? = legacyIdToLoader[legacyId]

    /**
     * Creates an Intent that will launch the correct Friendzoned module loader.
     *
     * @throws IllegalArgumentException if [legacyId] is not a known Friendzoned legacy ID.
     */
    fun intentFor(activity: ComponentActivity, legacyId: Int): Intent {
        val loaderClass = loaderClassFor(legacyId)
            ?: throw IllegalArgumentException("No Friendzoned loader for legacyId=$legacyId")
        return Intent(activity, loaderClass)
    }
}
