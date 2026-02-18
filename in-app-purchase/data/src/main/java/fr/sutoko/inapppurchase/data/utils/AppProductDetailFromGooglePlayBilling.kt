package fr.sutoko.inapppurchase.data.utils

import com.android.billingclient.api.ProductDetails
import fr.sutoko.inapppurchase.domain.model.AppProductDetails

object AppProductDetailFromGooglePlayBilling {

    /**
     * Executes the logic
     * @param productDetails : ProductDetails
     * @throws Exception if no pricing phases
     * @return AppProductDetails
     */
    fun execute(productDetails : ProductDetails) : AppProductDetails {
        return AppProductDetails(
            productId = productDetails.productId,
            price = productDetails.oneTimePurchaseOfferDetails?.formattedPrice,
        )
    }
}