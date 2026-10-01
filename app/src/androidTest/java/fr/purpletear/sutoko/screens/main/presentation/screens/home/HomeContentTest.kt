package fr.purpletear.sutoko.screens.main.presentation.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameMetadata
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.main.presentation.HomeCatalogState
import fr.purpletear.sutoko.sync.catalog.CatalogSyncStatus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeContentTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun fourStoriesRenderEvenWithoutAdditionalBanners() {
        compose.setContent { RenderHome(HomeCatalogState.fromGames(stories), CatalogSyncStatus.Ready) }
        stories.forEach { compose.onNodeWithText(it.metadata.title).assertIsDisplayed() }
        compose.onNodeWithTag("home_catalog_loading").assertDoesNotExist()
        compose.onNodeWithTag("home_catalog_status").assertDoesNotExist()
    }

    @Test
    fun cachedStoriesStayVisibleDuringRefreshAndOfflineFailure() {
        val sync = mutableStateOf(CatalogSyncStatus.Loading)
        compose.setContent { RenderHome(HomeCatalogState.fromGames(stories), sync.value) }
        compose.onNodeWithTag("home_catalog_loading").assertDoesNotExist()
        compose.runOnIdle { sync.value = CatalogSyncStatus.Failed }
        stories.forEach { compose.onNodeWithText(it.metadata.title).assertIsDisplayed() }
        compose.onNodeWithTag("home_catalog_status").assertDoesNotExist()
    }

    @Test
    fun offlineFirstLoadEndsPlaceholdersAndAllowsRetry() {
        val sync = mutableStateOf(CatalogSyncStatus.Loading)
        var retries = 0
        compose.setContent { RenderHome(HomeCatalogState.fromGames(emptyList()), sync.value, onRetry = { retries++ }) }
        compose.onNodeWithTag("home_catalog_loading").assertIsDisplayed()
        compose.runOnIdle { sync.value = CatalogSyncStatus.Failed }
        compose.onNodeWithTag("home_catalog_loading").assertDoesNotExist()
        compose.onNodeWithTag("home_catalog_status").assertIsDisplayed()
        compose.onNodeWithText(string(R.string.sutoko_home_catalog_retry)).performClick()
        compose.runOnIdle { assertEquals(1, retries) }
    }

    @Test
    fun successfulEmptyCatalogDoesNotShowPermanentPlaceholders() {
        compose.setContent { RenderHome(HomeCatalogState.fromGames(emptyList()), CatalogSyncStatus.Ready) }
        compose.onNodeWithTag("home_catalog_loading").assertDoesNotExist()
        compose.onNodeWithText(string(R.string.sutoko_home_catalog_empty)).assertIsDisplayed()
    }

    @Test
    fun balanceLoadingReservesSpaceWithoutMovingTheStoryRow() {
        val balance = mutableStateOf<Resource<Balance>>(Resource.Loading())
        compose.setContent { RenderHome(HomeCatalogState.fromGames(stories), CatalogSyncStatus.Ready, balance = balance.value) }
        compose.onNodeWithTag("home_coins").assertIsNotEnabled()
        val coinsBefore = compose.onNodeWithTag("home_coins").fetchSemanticsNode().boundsInRoot
        val storyTopBefore = compose.onNodeWithText("Story 1").fetchSemanticsNode().boundsInRoot.top
        compose.runOnIdle { balance.value = Resource.Success(Balance(coins = 1500, diamonds = 2000)) }
        val coinsAfter = compose.onNodeWithTag("home_coins").fetchSemanticsNode().boundsInRoot
        val storyTopAfter = compose.onNodeWithText("Story 1").fetchSemanticsNode().boundsInRoot.top
        assertEquals(coinsBefore.width, coinsAfter.width, 1f)
        assertEquals(storyTopBefore, storyTopAfter, 1f)
    }

    @Composable
    private fun RenderHome(
        catalog: HomeCatalogState,
        sync: CatalogSyncStatus,
        balance: Resource<Balance> = Resource.Loading(),
        onRetry: () -> Unit = {},
    ) {
        HomeContent(
            scrollState = rememberLazyListState(),
            catalog = catalog,
            catalogSyncStatus = sync,
            onRetryCatalog = onRetry,
            squareIcons = emptyMap(),
            favoriteIds = emptySet(),
            newChaptersSoonGameIds = emptySet(),
            coinsBalance = balance,
            isConnected = true,
            onAccountButtonPressed = {},
            onSignInButtonPressed = {},
            onCoinsButtonPressed = {},
            onDiamondsButtonPressed = {},
            onOptionsButtonPressed = {},
            onSquareStoryTap = {},
            onFullStoryTap = {},
            modifier = Modifier.fillMaxSize(),
        )
    }

    private val stories = (1..4).map {
        GameCatalog(id = "$it", metadata = GameMetadata(title = "Story $it"), canvasTechnologyRequiredVersion = 1)
    }

    private fun string(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
