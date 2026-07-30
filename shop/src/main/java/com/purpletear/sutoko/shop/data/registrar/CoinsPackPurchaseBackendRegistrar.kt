package com.purpletear.sutoko.shop.data.registrar

import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.shop.data.remote.RegisterOrderRequestDto
import com.purpletear.sutoko.shop.data.remote.ShopApi
import com.purpletear.sutoko.shop.data.remote.toDomainModel
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import fr.sutoko.inapppurchase.application.domain.PurchaseBackendRegistrar
import fr.sutoko.inapppurchase.application.domain.PurchaseRegistrationRejectedException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Claims Play Billing coins pack SKUs (`coins_pack_*`) and registers them on the
 * backend via `order/register`, which credits the coins to the user's account.
 * On success the locally cached balance is updated from the response so the UI
 * reflects the credit immediately. The server remains the validator: a
 * definitive 4xx answer means the purchase is rejected and the coordinator
 * purges the local state.
 */
@Singleton
class CoinsPackPurchaseBackendRegistrar @Inject constructor(
    private val shopApi: ShopApi,
    private val userRepository: UserRepository,
    private val shopRepository: ShopRepository,
) : PurchaseBackendRegistrar {

    override suspend fun supports(sku: String): Boolean {
        return sku.startsWith(COINS_PACK_SKU_PREFIX)
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
                response.isSuccessful -> {
                    response.body()?.let { shopRepository.updateBalance(it.toDomainModel()) }
                    Result.success(Unit)
                }

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
        const val COINS_PACK_SKU_PREFIX = "coins_pack_"
    }
}
