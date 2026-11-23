package com.purpletear.ads

import android.app.Activity

interface InterstitialGateway {
    val ready: Boolean
    fun preload()
    fun invalidateConsent()
    fun show(activity: Activity, gameId: String, placement: String, onShown: () -> Unit,
             onFinished: (shown: Boolean) -> Unit)
}
