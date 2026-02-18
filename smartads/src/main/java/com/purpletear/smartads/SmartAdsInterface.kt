package com.purpletear.smartads

interface SmartAdsInterface {
    fun onAdAborted()
    fun onAdSuccessfullyWatched()
    fun onAdRemovedPaid()
    fun onErrorFound(code: String?, message: String?, adUnit: String?)
}