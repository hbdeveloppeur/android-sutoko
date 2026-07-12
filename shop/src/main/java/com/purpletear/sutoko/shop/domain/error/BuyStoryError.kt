package com.purpletear.sutoko.shop.domain.error

sealed class BuyStoryError(message: String) : Exception(message) {
    class AlreadyOwned : BuyStoryError("Item already owned")
    class NotPurchasable : BuyStoryError("Item cannot be purchased")
    class Network(cause: Throwable) : BuyStoryError("Network error: ${cause.message}")
    class Unknown(message: String?) : BuyStoryError(message ?: "Unknown shop error")
}
