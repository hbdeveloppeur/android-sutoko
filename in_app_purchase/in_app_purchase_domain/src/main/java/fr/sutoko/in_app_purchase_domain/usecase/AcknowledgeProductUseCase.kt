package fr.sutoko.in_app_purchase_domain.usecase

import fr.sutoko.in_app_purchase_domain.model.AppPurchaseDetails
import fr.sutoko.in_app_purchase_domain.repository.BillingRepository
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