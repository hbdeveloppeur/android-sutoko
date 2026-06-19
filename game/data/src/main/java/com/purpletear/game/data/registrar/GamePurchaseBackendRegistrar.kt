package com.purpletear.game.data.registrar

import com.purpletear.game.data.remote.GameApi
import com.purpletear.sutoko.domain.exception.NotConnectedException
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import fr.sutoko.inapppurchase.application.domain.PurchaseBackendRegistrar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamePurchaseBackendRegistrar @Inject constructor(
    private val gameApi: GameApi,
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository,
) : PurchaseBackendRegistrar {

    override suspend fun supports(sku: String): Boolean {
        return gameRepository.observeOfficialGames().first().any { it.skus.contains(sku) }
    }

    override suspend fun register(
        sku: String,
        purchaseToken: String,
        orderId: String?
    ): Result<Unit> {
        val user = userRepository.observeUser().firstOrNull()
            ?: return Result.failure(NotConnectedException())

        val resolvedOrderId = orderId
            ?: return Result.failure(
                IllegalStateException("Game purchase registration requires orderId, but it was null")
            )

        return try {
            val response = gameApi.grantGame(
                userId = user.id,
                userToken = user.token,
                purchaseToken = purchaseToken,
                orderId = resolvedOrderId,
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    HttpException(response)
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
