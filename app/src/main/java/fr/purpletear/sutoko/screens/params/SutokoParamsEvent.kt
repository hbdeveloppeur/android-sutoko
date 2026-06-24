package fr.purpletear.sutoko.screens.params

sealed class SutokoParamsEvent {
    data object OnPrivacyPressed : SutokoParamsEvent()
    data object OnSharePressed : SutokoParamsEvent()
    data object OnReloadPressed : SutokoParamsEvent()
    data object OnDeletePressed : SutokoParamsEvent()
    data object OnDeleteDownloadedStoriesPressed : SutokoParamsEvent()
    data object OnDisconnectPressed : SutokoParamsEvent()
    data object OnBackPressed : SutokoParamsEvent()
    data object OnEffectConsumed : SutokoParamsEvent()
}
