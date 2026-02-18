package com.purpletear.game_data.provider

import android.content.Context
import java.io.File

class AndroidGamePathProviderImpl(private val context: Context) : GamePathProvider {
    companion object {
        private const val STORIES_DIRECTORY_NAME: String = "games"
    }

    override fun getStoriesDirectoryPath(): String {
        return File(
            context.filesDir,
            STORIES_DIRECTORY_NAME
        ).absolutePath
    }

    override fun getStoryDirectoryPath(storyId: Int): String {
        return getStoriesDirectoryPath() + File.separator + storyId
    }
}