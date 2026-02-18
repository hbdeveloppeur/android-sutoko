package fr.sutoko.in_app_purchase_domain.usecase

import fr.sutoko.in_app_purchase_domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveSubscriptionsSkus @Inject constructor(
    private val billingRepository: BillingRepository
) {
    suspend operator fun invoke(
    ): Flow<Result<List<String>>> {
        return billingRepository.getActiveSubscriptionsSkus(
        )
    }
}