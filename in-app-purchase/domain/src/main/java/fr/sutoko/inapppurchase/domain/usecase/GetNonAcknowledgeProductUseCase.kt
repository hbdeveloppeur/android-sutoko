package fr.sutoko.inapppurchase.domain.usecase

import fr.sutoko.inapppurchase.domain.model.AppPurchaseDetails
import fr.sutoko.inapppurchase.domain.repository.BillingRepository
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