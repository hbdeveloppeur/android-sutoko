package com.purpletear.platform.play.billing

import fr.sutoko.inapppurchase.billing.BillingProduct
import fr.sutoko.inapppurchase.billing.PurchaseReceipt
import fr.sutoko.inapppurchase.billing.PurchaseVerifier
import fr.sutoko.inapppurchase.billing.VerificationResult

internal class FakePurchaseVerifier : PurchaseVerifier {

    var result = VerificationResult(verified = true)

    override suspend fun verify(
        receipt: PurchaseReceipt,
        product: BillingProduct
    ): VerificationResult =
        result
}
