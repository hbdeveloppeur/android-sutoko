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
}
