package fr.sutoko.inapppurchase.application.domain.usecase

import com.purpletear.sutoko.domain.exception.NotConnectedException
import com.purpletear.sutoko.domain.repository.UserRepository
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PurchaseWithAuthCheckUseCase @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(sku: String): Result<Unit> {
        val isConnected = userRepository.observeIsConnected().first()
        if (!isConnected) {
            return Result.failure(NotConnectedException())
        }

        return purchaseRepository.purchase(sku)
    }
}
