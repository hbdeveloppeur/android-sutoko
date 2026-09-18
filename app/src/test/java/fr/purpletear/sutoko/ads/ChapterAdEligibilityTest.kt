package fr.purpletear.sutoko.ads

import com.purpletear.game.presentation.game_play.ads.ChapterAdEligibility
import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.shop.domain.repository.EntitlementRepository
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ChapterAdEligibilityTest {
    private val skus = listOf("story_complete")
    private val games = mockk<GameRepository>()
    private val entitlements = mockk<EntitlementRepository>()
    private val purchases = mockk<PurchaseRepository>()
    private val users = mockk<UserRepository>()
    private val logger = mockk<Logger>(relaxed = true)
    private val eligibility = ChapterAdEligibility(games, entitlements, purchases, users, logger)

    private fun ownership(granted: Boolean = false, purchased: Boolean = false, connected: Boolean = false) {
        val catalog = mockk<GameCatalog>()
        every { catalog.skus } returns skus
        every { games.observeGame("story") } returns flowOf(catalog)
        every { entitlements.observeIsGranted(skus) } returns flowOf(granted)
        every { purchases.observeIsPurchased(skus) } returns flowOf(purchased)
        every { users.isConnected() } returns Result.success(connected)
    }

    @Test
    fun completedLocalPurchaseBypassesAdsWithoutRefresh() = runTest {
        ownership(purchased = true, connected = true)
        assertFalse(eligibility.mayShow("story"))
        coVerify(exactly = 0) { entitlements.refreshGrant(any()) }
    }

    @Test
    fun localCoinGrantBypassesAdsWithoutRefresh() = runTest {
        ownership(granted = true, connected = true)
        assertFalse(eligibility.mayShow("story"))
        coVerify(exactly = 0) { entitlements.refreshGrant(any()) }
    }

    @Test
    fun signedInFreshGrantSuppressesAdDespiteStaleLocalOwnership() = runTest {
        ownership(connected = true)
        coEvery { entitlements.refreshGrant(skus) } returns Result.success(true)
        assertFalse(eligibility.mayShow("story"))
        coVerify(exactly = 1) { entitlements.refreshGrant(skus) }
    }

    @Test
    fun ownershipRefreshFailureSkipsAd() = runTest {
        ownership(connected = true)
        coEvery { entitlements.refreshGrant(skus) } returns Result.failure(IOException("offline"))
        assertFalse(eligibility.mayShow("story"))
    }

    @Test
    fun confirmedGuestWithoutPurchaseIsEligible() = runTest {
        ownership()
        assertTrue(eligibility.mayShow("story"))
        coVerify(exactly = 0) { entitlements.refreshGrant(any()) }
    }

    @Test
    fun ownershipObservationTimeoutSkipsAd() = runTest {
        ownership()
        every { games.observeGame("story") } returns flow { awaitCancellation() }
        assertFalse(eligibility.mayShow("story"))
        coVerify(exactly = 0) { entitlements.refreshGrant(any()) }
    }

    @Test
    fun ownershipRefreshTimeoutSkipsAd() = runTest {
        ownership(connected = true)
        coEvery { entitlements.refreshGrant(skus) } coAnswers { awaitCancellation() }
        assertFalse(eligibility.mayShow("story"))
    }
    @Test
    fun thrownOwnershipErrorSkipsAd() = runTest {
        ownership(connected = true)
        coEvery { entitlements.refreshGrant(skus) } throws IOException("connection reset")
        assertFalse(eligibility.mayShow("story"))
    }

}
