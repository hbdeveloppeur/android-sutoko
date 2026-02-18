package fr.sutoko.in_app_purchase_domain.usecase

import fr.sutoko.in_app_purchase_domain.model.AppPurchaseDetails
import fr.sutoko.in_app_purchase_domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNonAcknowledgeProductUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    suspend operator fun invoke(
    ): Flow<Result<List<AppPurchaseDetails>>> {
        return billingRepository.getNonAcknowledgePurchase(
        )
    }
}