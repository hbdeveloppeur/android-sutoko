package com.purpletear.game.presentation.model

sealed class ChapterState {
    data object Played : ChapterState()
    data object Current : ChapterState()
    data object Locked : ChapterState()
}