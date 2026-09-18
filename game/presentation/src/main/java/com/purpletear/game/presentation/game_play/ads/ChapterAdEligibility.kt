package com.purpletear.game.presentation.game_play.ads

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class ChapterAdEligibility @Inject constructor(
    private val games: GameRepository,
    private val entitlements: EntitlementRepository,
    private val purchases: PurchaseRepository,
    private val users: UserRepository,
    private val logger: Logger,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(gameId: String) = games.observeGame(gameId).flatMapLatest { catalog ->
        if (catalog == null) flowOf(false)
        else combine(
            entitlements.observeIsGranted(catalog.skus),
            purchases.observeIsPurchased(catalog.skus),
        ) { granted, purchased -> !granted && !purchased }
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
                true -> games.observeGame(gameId).first()?.let {
                    entitlements.refreshGrant(it.skus).getOrNull() == false
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
