package fr.sutoko.in_app_purchase_domain.usecase

import fr.sutoko.in_app_purchase_domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GooglePlayClientOwnsProductUseCase @Inject constructor(private val billingRepository: BillingRepository) {
    suspend operator fun invoke(sku: List<String>): Flow<Result<Map<String, String>>> =
        billingRepository.hasBoughtProduct(sku)
}
