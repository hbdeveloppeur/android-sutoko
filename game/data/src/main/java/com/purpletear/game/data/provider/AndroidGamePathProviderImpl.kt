package com.purpletear.game.data.provider

import android.content.Context
import java.io.File

class AndroidGamePathProviderImpl(private val context: Context) : AndroidGamePathProvider {
    companion object {
        private const val STORIES_DIRECTORY_NAME: String = "games"
    }

    override fun getStoriesDirectoryPath(): String {
        return File(
            context.filesDir,
            STORIES_DIRECTORY_NAME
        ).absolutePath
    }

    override fun getStoryDirectoryPath(storyId: String, legacyId: Int?): String {
        return getStoriesDirectoryPath() + File.separator + directoryNameFor(storyId, legacyId)
    }

    override fun getGamesDirectory(): File {
        return File(context.filesDir, STORIES_DIRECTORY_NAME)
    }

    override fun getGameDirectory(gameId: String, legacyId: Int?): File {
        return File(getGamesDirectory(), directoryNameFor(gameId, legacyId))
    }

    private fun directoryNameFor(gameId: String, legacyId: Int?): String {
        return legacyId?.toString() ?: gameId
    }
}
