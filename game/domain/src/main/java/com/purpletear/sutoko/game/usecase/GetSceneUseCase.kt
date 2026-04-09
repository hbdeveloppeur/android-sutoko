package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.scene.Scene
import com.purpletear.sutoko.game.repository.SceneRepository
import javax.inject.Inject

/**
 * Use case for retrieving a scene by its ID.
 * Returns the scene configuration or null if not found.
 */
class GetSceneUseCase @Inject constructor(
    private val sceneRepository: SceneRepository
) {
    suspend operator fun invoke(sceneId: Int): Scene? {
        return sceneRepository.getScene(sceneId)
    }
}
