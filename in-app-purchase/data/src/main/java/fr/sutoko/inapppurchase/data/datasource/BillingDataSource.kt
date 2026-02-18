package fr.sutoko.inapppurchase.data.datasource

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.sutoko.inapppurchase.data.utils.AppPurchaseDetailFromGooglePlayBilling
import fr.sutoko.inapppurchase.domain.model.AppPurchaseDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class BillingDataSource @Inject constructor(@ApplicationContext private val context: Context) {
    private val _purchases = MutableStateFlow<List<AppPurchaseDetails>>(mutableListOf())
    val purchases: StateFlow<List<AppPurchaseDetails>> = _purchases.asStateFlow()

    val client: BillingClient by lazy { build() }


    fun build(): BillingClient {
        return BillingClient.newBuilder(context)
            .setListener { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    var output = listOf<AppPurchaseDetails>()
                    for (purchase in purchases) {
                        output = output + AppPurchaseDetailFromGooglePlayBilling.execute(purchase)
                    }
                    _purchases.value = output
                } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // set an event to handle the cancellation
                } else {
                    // handle any other error codes.
                }
            }
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .build()
    }

}
