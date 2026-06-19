package com.purpletear.platform.play.billing

import com.android.billingclient.api.PurchasesUpdatedListener
import fr.sutoko.inapppurchase.billing.BillingClientWrapper
import fr.sutoko.inapppurchase.billing.BillingClientWrapperFactory

internal class FakeBillingClientWrapperFactory(
    val wrapper: FakeBillingClientWrapper = FakeBillingClientWrapper()
) : BillingClientWrapperFactory {

    override fun create(listener: PurchasesUpdatedListener): BillingClientWrapper {
        wrapper.purchasesUpdatedListener = listener
        return wrapper
    }
}
