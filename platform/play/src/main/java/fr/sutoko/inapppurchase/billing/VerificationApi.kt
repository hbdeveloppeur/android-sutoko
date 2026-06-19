package fr.sutoko.inapppurchase.billing

import androidx.annotation.Keep
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VerificationApi {

    @POST("register-purchase")
    suspend fun registerPurchase(
        @Body body: VerificationApiRequest
    ): Response<ResponseBody>
}

@Keep
data class VerificationApiRequest(
    val purchase_token: String,
    val product_id: String,
    val order_id: String? = null,
)