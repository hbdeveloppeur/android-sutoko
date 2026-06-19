package fr.sutoko.inapppurchase.billing

internal interface PurchaseVerifier {
    suspend fun verify(
        receipt: PurchaseReceipt,
        product: BillingProduct,
    ): VerificationResult
}