package fr.sutoko.inapppurchase.domain.usecase

import fr.sutoko.inapppurchase.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LaunchBillingFlowUseCase @Inject constructor(private val billingRepository: BillingRepository) {
    suspend operator fun invoke(identifiers : List<String>) : Flow<Result<Unit>> {
        return billingRepository.startBillingFlow(
            identifiers = identifiers
        )
    }
}