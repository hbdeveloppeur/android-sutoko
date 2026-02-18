package fr.sutoko.in_app_purchase_domain.usecase

import fr.sutoko.in_app_purchase_domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConsumePurchaseUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    suspend operator fun invoke(
        sku: String
    ): Flow<Result<Unit>> {
        return billingRepository.consumePurchase(
            sku = sku
        )
    }
}