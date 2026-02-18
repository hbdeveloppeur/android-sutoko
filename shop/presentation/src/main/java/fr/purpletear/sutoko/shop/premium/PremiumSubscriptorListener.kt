package fr.purpletear.sutoko.shop.premium

interface PremiumSubscriptorListener {
    fun onBillingServicesNotAvailable()
    fun onConnectionFailed()
    fun onUnhandledError()
    fun onSubscriptionGrant()
}