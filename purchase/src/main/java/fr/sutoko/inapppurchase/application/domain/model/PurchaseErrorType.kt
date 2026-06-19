package fr.sutoko.inapppurchase.application.domain.model

import fr.sutoko.inapppurchase.billing.exception.PurchaseAlreadyOwnedException
import fr.sutoko.inapppurchase.billing.exception.PurchaseCancelledException
import fr.sutoko.inapppurchase.billing.exception.PurchaseFailedException
import fr.sutoko.inapppurchase.billing.exception.PurchasePendingException

enum class PurchaseErrorType {
    PENDING,
    CANCELLED,
    ALREADY_OWNED,
    FAILED,
    UNKNOWN,
}

fun Throwable.toPurchaseErrorType(): PurchaseErrorType = when (this) {
    is PurchasePendingException -> PurchaseErrorType.PENDING
    is PurchaseCancelledException -> PurchaseErrorType.CANCELLED
    is PurchaseAlreadyOwnedException -> PurchaseErrorType.ALREADY_OWNED
    is PurchaseFailedException -> PurchaseErrorType.FAILED
    else -> PurchaseErrorType.UNKNOWN
}
