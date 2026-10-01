package fr.purpletear.sutoko.screens.main.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sharedelements.theme.SutokoTypography
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.game.presentation.game_catalog.GameCardPlaceholder
import com.purpletear.game.presentation.game_catalog.GamePosterRowPlaceholder
import com.purpletear.game.presentation.game_catalog.GameSquaresPlaceholder
import com.purpletear.game.presentation.game_catalog.GameCard
import com.purpletear.game.presentation.game_catalog.GamePosterRow
import com.purpletear.game.presentation.game_catalog.GameSquares
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.main.presentation.HomeCatalogState
import fr.purpletear.sutoko.screens.main.presentation.HomeScreenViewModel
import fr.purpletear.sutoko.screens.main.presentation.MainScreenPages
import fr.purpletear.sutoko.screens.main.presentation.screens.TopNavigation
import fr.purpletear.sutoko.screens.main.presentation.screens.home.components.HomeEntrance
import fr.purpletear.sutoko.screens.main.presentation.screens.home.components.rememberHomeContentEntrance
import fr.purpletear.sutoko.sync.catalog.CatalogSyncStatus

/**
 * Home screen composable that displays the main content of the application.
 *
 * @param mainNavController The navigation controller for handling navigation events
 * @param viewModel The ViewModel that manages the screen state and business logic
 */
@Composable
fun HomeScreen(
    mainNavController: NavController,
    onAccountPressed: () -> Unit,
    onSignInPressed: () -> Unit,
    onOptionsPressed: () -> Unit,
    onCoinsPressed: () -> Unit,
    onDiamondsPressed: () -> Unit,
    viewModel: HomeScreenViewModel
) {
    val scrollState = rememberLazyListState()
    val systemUiController = rememberSystemUiController()

    // System UI settings
    LaunchedEffect(Unit) {
        systemUiController.isStatusBarVisible = true
    }

    val catalogSyncStatus = viewModel.catalogSyncStatus.collectAsStateWithLifecycle()
    val balance = viewModel.balance.collectAsStateWithLifecycle()
    val isConnected = viewModel.isConnected.collectAsStateWithLifecycle()
    val favoriteIds = viewModel.favoriteIds.collectAsStateWithLifecycle()
    val newChaptersSoonGameIds = viewModel.newChaptersSoonGameIds.collectAsStateWithLifecycle()

    HomeContent(
        scrollState = scrollState,
        catalog = viewModel.catalog.value,
        catalogSyncStatus = catalogSyncStatus.value,
        onRetryCatalog = viewModel::retryCatalog,
        squareIcons = viewModel.squareIcons,
        favoriteIds = favoriteIds.value,
        newChaptersSoonGameIds = newChaptersSoonGameIds.value,
        coinsBalance = balance.value,
        isConnected = isConnected.value,
        onAccountButtonPressed = onAccountPressed,
        onSignInButtonPressed = onSignInPressed,
        onCoinsButtonPressed = onCoinsPressed,
        onDiamondsButtonPressed = onDiamondsPressed,
        onOptionsButtonPressed = onOptionsPressed,
        onSquareStoryTap = { card ->
            mainNavController.navigate(MainScreenPages.GamePreview.createRoute(card.id))
        },
        onFullStoryTap = { card ->
            mainNavController.navigate(MainScreenPages.GamePreview.createRoute(card.id))
        }
    )
}

/**
 * Stateless HomeContent composable for better testability and preview support.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun HomeContent(
    scrollState: LazyListState,
    catalog: HomeCatalogState,
    catalogSyncStatus: CatalogSyncStatus,
    onRetryCatalog: () -> Unit,
    squareIcons: Map<Int, Int?>,
    favoriteIds: Set<String>,
    newChaptersSoonGameIds: Set<String>,
    coinsBalance: Resource<Balance>,
    isConnected: Boolean?,
    onAccountButtonPressed: () -> Unit,
    onSignInButtonPressed: () -> Unit,
    onCoinsButtonPressed: () -> Unit,
    onDiamondsButtonPressed: () -> Unit,
    onOptionsButtonPressed: () -> Unit,
    onSquareStoryTap: (GameCatalog) -> Unit,
    onFullStoryTap: (GameCatalog) -> Unit,
    modifier: Modifier = Modifier
) {
    val animateContent = rememberHomeContentEntrance(catalog.hasStories)
    val isLoading = !catalog.isCacheLoaded || (!catalog.hasStories && catalogSyncStatus == CatalogSyncStatus.Loading)
    LazyColumn(
        state = scrollState,
        modifier = modifier
            .semantics { testTagsAsResourceId = true }
            .testTag("home_screen")
            .statusBarsPadding()
    ) {
        topNavigationSection(
            balance = coinsBalance,
            isConnected = isConnected,
            onAccountButtonPressed = onAccountButtonPressed,
            onSignInButtonPressed = onSignInButtonPressed,
            onCoinsButtonPressed = onCoinsButtonPressed,
            onDiamondsButtonPressed = onDiamondsButtonPressed,
            onOptionsButtonPressed = onOptionsButtonPressed
        )

        if (isLoading) {
            item(key = "square_stories", contentType = "squares") { GameSquaresPlaceholder(Modifier.testTag("home_catalog_loading")) }
            releaseScheduleTitleSection(visible = true, animate = false)
            item(key = "vertical_stories", contentType = "posters") {
                GamePosterRowPlaceholder(Modifier.padding(vertical = 8.dp))
            }
            items(2, key = { "loading_card_$it" }, contentType = { "card" }) {
                GameCardPlaceholder()
            }
        } else if (!catalog.hasStories) {
            item(key = "catalog_status") {
                CatalogStatus(
                    failed = catalogSyncStatus == CatalogSyncStatus.Failed,
                    onRetry = onRetryCatalog,
                )
            }
        } else {
            squareStoriesSection(
                squareStories = catalog.squareStories,
                squareIcons = squareIcons,
                animate = animateContent,
                onStoryTap = onSquareStoryTap,
            )
            releaseScheduleTitleSection(visible = catalog.fullStories.isNotEmpty(), animate = animateContent)
            verticalStoriesSection(
                verticalStories = catalog.verticalStories,
                favoriteIds = favoriteIds,
                animate = animateContent,
                onStoryTap = onFullStoryTap,
            )
            fullStoriesSection(
                fullStories = catalog.fullStories,
                favoriteIds = favoriteIds,
                newChaptersSoonGameIds = newChaptersSoonGameIds,
                animate = animateContent,
                onStoryTap = onFullStoryTap,
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

private fun LazyListScope.topNavigationSection(
    balance: Resource<Balance>,
    isConnected: Boolean?,
    onAccountButtonPressed: () -> Unit,
    onSignInButtonPressed: () -> Unit,
    onCoinsButtonPressed: () -> Unit,
    onDiamondsButtonPressed: () -> Unit,
    onOptionsButtonPressed: () -> Unit
) {
    item(key = "top_navigation", contentType = "navigation") {
        TopNavigation(
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
                .padding(start = 8.dp),
            balance = balance,
            isConnected = isConnected,
            onAccountButtonPressed = onAccountButtonPressed,
            onSignInButtonPressed = onSignInButtonPressed,
            onCoinsButtonPressed = onCoinsButtonPressed,
            onDiamondsButtonPressed = onDiamondsButtonPressed,
            onOptionsButtonPressed = onOptionsButtonPressed
        )
    }
}

private fun LazyListScope.squareStoriesSection(
    squareStories: List<GameCatalog>,
    squareIcons: Map<Int, Int?>,
    animate: Boolean,
    onStoryTap: (GameCatalog) -> Unit,
) {
    if (squareStories.isEmpty()) return
    item(key = "square_stories", contentType = "squares") {
        HomeEntrance(animate = animate) {
            GameSquares(stories = squareStories, icons = squareIcons, onTap = onStoryTap)
        }
    }
}

private fun LazyListScope.verticalStoriesSection(
    verticalStories: List<GameCatalog>,
    favoriteIds: Set<String>,
    animate: Boolean,
    onStoryTap: (GameCatalog) -> Unit
) {
    if (verticalStories.isEmpty()) return

    item(key = "vertical_stories", contentType = "posters") {
        HomeEntrance(animate = animate, delayMillis = 60) {
            GamePosterRow(
                modifier = Modifier.padding(vertical = 8.dp),
                stories = verticalStories,
                favoriteIds = favoriteIds,
                onTap = onStoryTap,
            )
        }
    }
}

private fun LazyListScope.releaseScheduleTitleSection(visible: Boolean, animate: Boolean) {
    if (!visible) return
    item(key = "release_schedule_title", contentType = "title") {
        HomeEntrance(animate = animate, delayMillis = 30) {
            Text(
                text = stringResource(R.string.sutoko_main_section_title_release_schedule),
                fontSize = 14.sp,
                style = SutokoTypography.body1.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFFFAFAFA)
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            )
        }
    }
}

private fun LazyListScope.fullStoriesSection(
    fullStories: List<GameCatalog>,
    favoriteIds: Set<String>,
    newChaptersSoonGameIds: Set<String>,
    animate: Boolean,
    onStoryTap: (GameCatalog) -> Unit
) {
    if (fullStories.isEmpty()) return

    itemsIndexed(
        items = fullStories,
        key = { _, item -> "card_${item.id}" },
        contentType = { _, _ -> "card" },
    ) { index, item ->
        HomeEntrance(
            animate = animate,
            delayMillis = 80 + index.coerceAtMost(2) * 30,
            modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
        ) {
            GameCard(
                gameCatalog = item,
                isFavorite = item.id in favoriteIds,
                hasNewChaptersSoon = item.id in newChaptersSoonGameIds,
                onTap = onStoryTap,
            )
        }
    }
}

@Composable
private fun CatalogStatus(failed: Boolean, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.testTag("home_catalog_status").fillMaxWidth().padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(if (failed) R.string.sutoko_home_catalog_error else R.string.sutoko_home_catalog_empty),
            color = Color.White.copy(alpha = 0.75f),
            style = SutokoTypography.body1,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.sutoko_home_catalog_retry), color = Color(0xFFFF447C))
        }
    }
}
