package com.purpletear.ads

interface AdsConfiguration {
    val interstitialUnitId: String
    val enabled: Boolean
    val rolloutPercent: Int
    fun placementEnabled(placement: String): Boolean
}
