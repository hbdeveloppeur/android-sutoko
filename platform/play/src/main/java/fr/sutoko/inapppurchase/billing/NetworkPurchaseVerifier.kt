package fr.sutoko.inapppurchase.billing

import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class NetworkPurchaseVerifier @Inject constructor(
    private val api: VerificationApi,
) : PurchaseVerifier {

    override suspend fun verify(
        receipt: PurchaseReceipt,
        product: BillingProduct,
    ): VerificationResult {
        return try {
            val response = api.registerPurchase(
                VerificationApiRequest(
                    purchase_token = receipt.purchaseToken,
                    product_id = product.sku,
                    order_id = receipt.orderId,
                )
            )

            when (response.code()) {
                200 -> VerificationResult(verified = true)
                400 -> VerificationResult(
                    verified = false,
                    message = "Invalid purchase"
                )

                else -> VerificationResult(
                    verified = true,
                    message = "Verification server returned ${response.code()}"
                )
            }
        } catch (e: HttpException) {
            VerificationResult(
                verified = true,
                message = "Verification server returned ${e.code()}"
            )
        } catch (e: IOException) {
            VerificationResult(
                verified = true,
                message = e.message ?: "Verification request failed"
            )
        }
    }
}
