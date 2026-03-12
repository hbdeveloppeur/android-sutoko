package com.purpletear.game.data.provider

import java.io.File

interface GamePathProvider {

    fun getStoriesDirectoryPath(): String
    fun getStoryDirectoryPath(storyId: String): String
    fun getGamesDirectory(): File
}
