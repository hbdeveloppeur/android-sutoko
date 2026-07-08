package com.purpletear.game.presentation.game_preview

import androidx.annotation.Keep
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.game.model.game.GameCatalog

sealed interface GamePreviewUiState {
    data object Loading : GamePreviewUiState

    @Keep
    data class Data(
        val item: GameItem,
        val gameCatalog: GameCatalog
    ) : GamePreviewUiState

    data object NotFound : GamePreviewUiState

    @Keep
    data class Error(val error: GameUiError) : GamePreviewUiState
}
