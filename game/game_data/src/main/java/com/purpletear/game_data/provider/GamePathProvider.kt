package com.purpletear.game_data.provider

interface GamePathProvider {

    fun getStoriesDirectoryPath(): String
    fun getStoryDirectoryPath(storyId: Int): String
}