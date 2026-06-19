package fr.sutoko.inapppurchase.billing.exception

class PurchaseSyncException(
    val causes: List<Throwable>
) : Exception("Failed to fully sync purchases. Failure count=${causes.size}")