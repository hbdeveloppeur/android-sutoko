package com.purpletear.game.presentation.game_preview

sealed class GamePreviewAction {
    data object OnBuy : GamePreviewAction()
    data object OnBuyConfirm : GamePreviewAction()
    data object OnDownload : GamePreviewAction()
    data object OnUpdateGame : GamePreviewAction()
    data object OnUpdateApp : GamePreviewAction()
    data object OnAbortBuy : GamePreviewAction()
    data object OnRestart : GamePreviewAction()
    data object OnPlay : GamePreviewAction()
    data object OnDelete : GamePreviewAction()
}