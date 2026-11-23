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
        return getGameDirectory(storyId, legacyId).absolutePath
    }

    override fun getGamesDirectory(): File {
        return File(context.filesDir, STORIES_DIRECTORY_NAME)
    }

    override fun getGameDirectory(gameId: String, legacyId: Int?): File {
        val directoryName = legacyId?.toString() ?: gameId
        require(directoryName.isNotBlank() && directoryName == File(directoryName).name) {
            "Invalid game directory name"
        }
        val gamesDirectory = getGamesDirectory().canonicalFile
        val gameDirectory = File(gamesDirectory, directoryName).canonicalFile
        require(gameDirectory.parentFile == gamesDirectory) { "Game directory outside story storage" }
        return gameDirectory
    }
}
