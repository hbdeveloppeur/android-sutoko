package com.purpletear.game.presentation.game_preview

enum class GamePreviewRestartTarget { CHAPTER, GAME }

sealed interface GamePreviewRestartResult {
    val target: GamePreviewRestartTarget

    data class Success(override val target: GamePreviewRestartTarget) : GamePreviewRestartResult
    data class Failure(override val target: GamePreviewRestartTarget) : GamePreviewRestartResult
}
