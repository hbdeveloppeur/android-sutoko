package com.purpletear.game.presentation.game_play

import androidx.lifecycle.ViewModel
import com.purpletear.game.data.provider.GamePathProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for SceneComposable that provides path resolution.
 */
@HiltViewModel
class SceneComposableViewModel @Inject constructor(
    private val gamePathProvider: GamePathProvider
) : ViewModel() {

    /**
     * Resolves a storage path to a full local file path.
     *
     * @param gameId The game/story ID
     * @param storagePath The relative storage path from SceneAsset
     * @return The full absolute path to the file, or null if gameId is null
     */
    fun resolveAssetPath(gameId: String, storagePath: String): String {
        val fileName = storagePath.substringAfterLast("/")
        val basePath = gamePathProvider.getStoryDirectoryPath(gameId)
        return "$basePath${File.separator}assets${File.separator}$fileName"
    }
}
