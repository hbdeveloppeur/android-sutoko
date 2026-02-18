package com.purpletear.game.data.provider

interface GamePathProvider {

    fun getStoriesDirectoryPath(): String
    fun getStoryDirectoryPath(storyId: Int): String
}