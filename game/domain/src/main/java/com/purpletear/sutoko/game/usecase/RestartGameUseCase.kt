package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.repository.ChapterRepository
import javax.inject.Inject

class RestartGameUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) {
    suspend operator fun invoke(gameId: String) {
        chapterRepository.restart(gameId)
    }
}
