package com.purpletear.sutoko.shop.domain.usecase

import com.purpletear.sutoko.shop.domain.model.PackItem
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class GetShopPackPricesUseCase @Inject constructor(
    private val getShopPacksUseCase: GetShopPacksUseCase,
    private val purchaseRepository: PurchaseRepository,
) {

    suspend operator fun invoke(): Result<List<PackItem>> {
        val packs = try {
            getShopPacksUseCase().getOrThrow()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return Result.failure(e)
        }

        val productBySku = packs.associate { pack ->
            val product = try {
                purchaseRepository.queryProductDetails(pack.sku).getOrNull()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                null
            }
            pack.sku to product
        }

        return Result.success(
            packs.map { pack ->
                PackItem(
                    pack = pack,
                    formattedPrice = productBySku[pack.sku]?.formattedPrice,
                )
            }
        )
    }
}
