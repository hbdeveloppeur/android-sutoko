package com.purpletear.aiconversation.data.registrar

import com.purpletear.aiconversation.domain.repository.AiConversationShopRepository
import com.purpletear.sutoko.domain.exception.NotConnectedException
import com.purpletear.sutoko.domain.repository.UserRepository
import fr.sutoko.inapppurchase.application.domain.PurchaseBackendRegistrar
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registers AI message pack purchases on the backend so the bought tokens are
 * credited to the user. Claimed SKUs are the pack identifiers cached by the
 * last successful packs fetch. Driven by the purchase module's
 * PurchaseBackendRegistrationCoordinator, which provides retry with backoff.
 */
@Singleton
class AiMessagePackPurchaseBackendRegistrar @Inject constructor(
    private val aiConversationShopRepository: AiConversationShopRepository,
    private val userRepository: UserRepository,
) : PurchaseBackendRegistrar {

    override suspend fun supports(sku: String): Boolean {
        return aiConversationShopRepository.getCachedMessagePackIdentifiers().contains(sku)
    }

    override suspend fun register(
        sku: String,
        purchaseToken: String,
        orderId: String?
    ): Result<Unit> {
        val user = userRepository.observeUser().firstOrNull()
            ?: return Result.failure(NotConnectedException())

        val resolvedOrderId = orderId
            ?: return Result.failure(
                IllegalStateException("AI pack purchase registration requires orderId, but it was null")
            )

        return aiConversationShopRepository.buyMessagePack(
            userId = user.id,
            userToken = user.token,
            packId = sku,
            orderId = resolvedOrderId,
            purchaseToken = purchaseToken,
            productId = sku,
        ).map { }
    }
}
