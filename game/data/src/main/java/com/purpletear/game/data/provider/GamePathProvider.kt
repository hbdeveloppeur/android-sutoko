package com.purpletear.game.data.provider

import com.purpletear.sutoko.game.provider.GamePathProvider
import java.io.File

/**
 * Extended GamePathProvider with data-layer specific operations.
 */
interface AndroidGamePathProvider : GamePathProvider {

    /**
     * Returns the File object for the games directory.
     */
    fun getGamesDirectory(): File

    /**
     * Returns the File object for a specific game's directory.
     *
     * @param gameId The unique identifier for the game.
     * @param legacyId The legacy integer identifier, if any.
     */
    fun getGameDirectory(gameId: String, legacyId: Int? = null): File
}
