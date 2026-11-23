package com.purpletear.ads

import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.core.domain.logger.exception
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.shop.domain.repository.EntitlementRepository
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

internal const val REMOVE_ADS_SKU = "fr_purpletear_sutoko_iap_removeads"

class AdEligibility @Inject constructor(
    private val games: GameRepository,
    private val entitlements: EntitlementRepository,
    private val purchases: PurchaseRepository,
    private val users: UserRepository,
    private val logger: Logger,
) {
    private fun catalog(gameId: String) = if (gameId.startsWith("legacy:")) {
        val legacyId = gameId.removePrefix("legacy:").toIntOrNull()
        games.observeOfficialGames().map { rows -> rows.firstOrNull { it.legacyId == legacyId && legacyId != null } }
    } else games.observeGame(gameId)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(gameId: String) = combine(
        users.observeIsConnected(), users.observeUser().map { it?.id },
    ) { connected, id -> connected to id }.distinctUntilChanged().flatMapLatest {
        catalog(gameId).flatMapLatest { catalog ->
            if (catalog == null) flowOf(false)
            else {
                val skus = (catalog.skus + REMOVE_ADS_SKU).distinct()
                combine(entitlements.observeIsGranted(skus), purchases.observeIsPurchased(skus)) {
                        granted, purchased -> !granted && !purchased
                }
            }
        }
    }.catch { error ->
        logger.exception(error) { "Chapter ad ownership lookup failed" }
        emit(false)
    }

    /** Uncertain ownership bypasses ads; it never grants download or purchase rights. */
    suspend fun mayShow(gameId: String): Boolean = try {
        withTimeoutOrNull(5_000L) {
            if (!observe(gameId).first()) return@withTimeoutOrNull false
            when (users.isConnected().getOrNull()) {
                false -> true
                true -> catalog(gameId).first()?.let {
                    entitlements.refreshGrant((it.skus + REMOVE_ADS_SKU).distinct()).getOrNull() == false
                } ?: false
                null -> false
            }
        } == true
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        logger.exception(error) { "Chapter ad ownership refresh failed" }
        false
    }
}
