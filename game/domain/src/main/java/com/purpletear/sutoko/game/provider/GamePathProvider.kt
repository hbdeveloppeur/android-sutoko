package com.purpletear.sutoko.game.provider

/**
 * Interface for providing game file system paths.
 * Implemented by the data layer.
 */
interface GamePathProvider {

    /**
     * Returns the base directory path for all stories/games.
     */
    fun getStoriesDirectoryPath(): String

    /**
     * Returns the directory path for a specific story/game.
     * @param storyId The unique identifier for the story/game
     */
    fun getStoryDirectoryPath(storyId: String): String
}
