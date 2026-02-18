package fr.sutoko.in_app_purchase_domain.usecase

import android.app.Activity
import fr.sutoko.in_app_purchase_domain.model.AppProductDetails
import fr.sutoko.in_app_purchase_domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StartBillingFlowUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    suspend operator fun invoke(
        activity: Activity,
        identifiers : List<String>
    ): Flow<Result<List<AppProductDetails>>> {
        return billingRepository.getProducts(
            identifiers = identifiers
        )
    }
}