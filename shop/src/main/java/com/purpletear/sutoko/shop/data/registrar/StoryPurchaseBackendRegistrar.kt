package com.purpletear.sutoko.shop.data.registrar

import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.shop.data.remote.RegisterOrderRequestDto
import com.purpletear.sutoko.shop.data.remote.ShopApi
import fr.sutoko.inapppurchase.application.domain.PurchaseBackendRegistrar
import fr.sutoko.inapppurchase.application.domain.PurchaseRegistrationRejectedException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Claims Play Billing story and premium SKUs and registers them on the backend via
 * `order/register`. The backend derives premium from any acknowledged SKU whose
 * identifier contains "premium", so premium purchases must go through the same
 * registration pipe. The server remains the validator: a definitive 4xx answer
 * means the purchase is rejected and the coordinator purges the local state.
 */
@Singleton
class StoryPurchaseBackendRegistrar @Inject constructor(
    private val shopApi: ShopApi,
    private val userRepository: UserRepository,
) : PurchaseBackendRegistrar {

    override suspend fun supports(sku: String): Boolean {
        return sku.matches(STORY_SKU_REGEX) || sku.contains(PREMIUM_SKU_MARKER, ignoreCase = true)
    }

    override suspend fun register(
        sku: String,
        purchaseToken: String,
        orderId: String?
    ): Result<Unit> {
        // A paid purchase must be registered eventually: wait for the session
        // instead of failing, since retrying a missing session cannot succeed.
        val user = userRepository.observeUser().filterNotNull().first()

        return try {
            val response = shopApi.registerOrder(
                RegisterOrderRequestDto(
                    purchaseToken = purchaseToken,
                    skuIdentifier = sku,
                    userId = user.id,
                    userToken = user.token,
                )
            )
            val code = response.code()
            when {
                response.isSuccessful -> Result.success(Unit)
                code in 400..499 && code != 408 && code != 429 -> Result.failure(
                    PurchaseRegistrationRejectedException("order/register rejected $sku: HTTP $code")
                )

                else -> Result.failure(IOException("order/register failed for $sku: HTTP $code"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        val STORY_SKU_REGEX = Regex("^story_\\d+$")
        const val PREMIUM_SKU_MARKER = "premium"
    }
}
