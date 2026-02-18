package fr.sutoko.inapppurchase.domain.usecase

import fr.sutoko.inapppurchase.domain.model.AppPurchaseDetails
import fr.sutoko.inapppurchase.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AcknowledgeProductUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    suspend operator fun invoke(
        purchaseDetails : AppPurchaseDetails,
        consume : Boolean
    ): Flow<Result<Unit>> {
        return billingRepository.acknowledgePurchase(
            purchaseDetails,
            consume
        )
    }
}