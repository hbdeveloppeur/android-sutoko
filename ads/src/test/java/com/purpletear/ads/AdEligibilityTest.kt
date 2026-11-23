package com.purpletear.ads

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AdEligibilityTest {
    private val skus = listOf("story_complete")
    private val adSkus = skus + REMOVE_ADS_SKU
    private val games = mockk<GameRepository>()
    private val entitlements = mockk<EntitlementRepository>()
    private val purchases = mockk<PurchaseRepository>()
    private val users = mockk<UserRepository>()
    private val logger = mockk<Logger>(relaxed = true)
    private val eligibility = AdEligibility(games, entitlements, purchases, users, logger)

    private fun ownership(granted: Boolean = false, purchased: Boolean = false, connected: Boolean = false) {
        val catalog = mockk<GameCatalog>()
        every { catalog.skus } returns skus
        every { games.observeGame("story") } returns flowOf(catalog)
        every { entitlements.observeIsGranted(adSkus) } returns flowOf(granted)
        every { purchases.observeIsPurchased(adSkus) } returns flowOf(purchased)
        every { users.isConnected() } returns Result.success(connected)
        every { users.observeUser() } returns flowOf(null)
        every { users.observeIsConnected() } returns flowOf(connected)
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
        coEvery { entitlements.refreshGrant(adSkus) } returns Result.success(true)
        assertFalse(eligibility.mayShow("story"))
        coVerify(exactly = 1) { entitlements.refreshGrant(adSkus) }
    }

    @Test
    fun ownershipRefreshFailureSkipsAd() = runTest {
        ownership(connected = true)
        coEvery { entitlements.refreshGrant(adSkus) } returns Result.failure(IOException("offline"))
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
        coEvery { entitlements.refreshGrant(adSkus) } coAnswers { awaitCancellation() }
        assertFalse(eligibility.mayShow("story"))
    }
    @Test
    fun thrownOwnershipErrorSkipsAd() = runTest {
        ownership(connected = true)
        coEvery { entitlements.refreshGrant(adSkus) } throws IOException("connection reset")
        assertFalse(eligibility.mayShow("story"))
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun accountChangeReemitsOwnershipEvenWhenTheLocalGrantIsStillFalse() = runTest {
        ownership()
        val user = MutableStateFlow<com.purpletear.sutoko.domain.model.User?>(null)
        every { users.observeUser() } returns user
        val values = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            eligibility.observe("story").take(2).toList(values)
        }
        user.value = mockk { every { id } returns "test_member" }
        assertTrue(values == listOf(true, true))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun connectionChangeInvalidatesGuestEvenBeforeProfileLoads() = runTest {
        ownership()
        val connected = MutableStateFlow(false)
        every { users.observeIsConnected() } returns connected
        val values = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            eligibility.observe("story").take(2).toList(values)
        }
        connected.value = true
        assertTrue(values == listOf(true, true))
    }
}
