package com.purpletear.game.presentation.states

internal sealed class StoryPreviewAction {
    data object OnBuy : StoryPreviewAction()
    data object OnBuyConfirm : StoryPreviewAction()
    data object OnDownload : StoryPreviewAction()
    data object OnReload : StoryPreviewAction()
    data object OnUpdateGame : StoryPreviewAction()
    data object OnUpdateApp : StoryPreviewAction()
    data object OnAbortBuy : StoryPreviewAction()
    data object OnRestart : StoryPreviewAction()
    data object OnPlay : StoryPreviewAction()
}