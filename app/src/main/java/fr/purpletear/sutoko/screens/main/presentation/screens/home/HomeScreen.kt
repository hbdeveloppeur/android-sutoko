package fr.purpletear.sutoko.screens.main.presentation.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sharedelements.theme.SutokoTypography
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.game.presentation.game_catalog.GameCard
import com.purpletear.game.presentation.game_catalog.GamePosterRow
import com.purpletear.game.presentation.game_catalog.GameSquares
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.main.presentation.HomeScreenViewModel
import fr.purpletear.sutoko.screens.main.presentation.MainScreenPages
import fr.purpletear.sutoko.screens.main.presentation.screens.TopNavigation

/**
 * Home screen composable that displays the main content of the application.
 *
 * @param mainNavController The navigation controller for handling navigation events
 * @param viewModel The ViewModel that manages the screen state and business logic
 */
@OptIn(ExperimentalFoundationApi::class)
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

    val balance = viewModel.balance.collectAsStateWithLifecycle()
    val isConnected = viewModel.isConnected.collectAsStateWithLifecycle()
    val favoriteIds = viewModel.favoriteIds.collectAsStateWithLifecycle()
    val newChaptersSoonGameIds = viewModel.newChaptersSoonGameIds.collectAsStateWithLifecycle()

    HomeContent(
        scrollState = scrollState,
        squareStories = viewModel.squareStories.value,
        fullStories = viewModel.fullStories.value,
        verticalStories = viewModel.verticalStories.value,
        squareIcons = viewModel.squareIcons.value,
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
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun HomeContent(
    scrollState: LazyListState,
    squareStories: List<GameCatalog>,
    fullStories: List<GameCatalog>,
    verticalStories: List<GameCatalog>,
    squareIcons: Map<Int, Int?>,
    favoriteIds: Set<String>,
    newChaptersSoonGameIds: Set<String>,
    coinsBalance: Resource<Balance>,
    isConnected: Boolean,
    onAccountButtonPressed: () -> Unit,
    onSignInButtonPressed: () -> Unit,
    onCoinsButtonPressed: () -> Unit,
    onDiamondsButtonPressed: () -> Unit,
    onOptionsButtonPressed: () -> Unit,
    onSquareStoryTap: (GameCatalog) -> Unit,
    onFullStoryTap: (GameCatalog) -> Unit,
    modifier: Modifier = Modifier
) {
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

        squareStoriesSection(
            squareStories = squareStories,
            fullStories = fullStories,
            squareIcons = squareIcons,
            onStoryTap = onSquareStoryTap
        )


        releaseScheduleTitleSection(fullStories = fullStories)

        verticalStoriesSection(
            verticalStories = verticalStories,
            favoriteIds = favoriteIds,
            onStoryTap = onFullStoryTap
        )

        fullStoriesSection(
            fullStories = fullStories,
            favoriteIds = favoriteIds,
            newChaptersSoonGameIds = newChaptersSoonGameIds,
            onStoryTap = onFullStoryTap
        )

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

private fun LazyListScope.topNavigationSection(
    balance: Resource<Balance>,
    isConnected: Boolean,
    onAccountButtonPressed: () -> Unit,
    onSignInButtonPressed: () -> Unit,
    onCoinsButtonPressed: () -> Unit,
    onDiamondsButtonPressed: () -> Unit,
    onOptionsButtonPressed: () -> Unit
) {
    item(key = "top_navigation") {
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

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.squareStoriesSection(
    squareStories: List<GameCatalog>,
    fullStories: List<GameCatalog>,
    squareIcons: Map<Int, Int?>,
    onStoryTap: (GameCatalog) -> Unit
) {
    if (squareStories.isEmpty() || fullStories.isEmpty()) return

    item(key = "square_stories") {
        GameSquares(
            stories = squareStories,
            icons = squareIcons,
            onTap = onStoryTap
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.squareStoriesAsCardsSection(
    squareStories: List<GameCatalog>,
    fullStories: List<GameCatalog>,
    favoriteIds: Set<String>,
    newChaptersSoonGameIds: Set<String>,
    onStoryTap: (GameCatalog) -> Unit
) {
    if (squareStories.isEmpty() || fullStories.isNotEmpty()) return

    itemsIndexed(
        items = squareStories,
        key = { _, item -> "card_${item.id}" }
    ) { _, item ->
        GameCard(
            modifier = Modifier.animateItemPlacement(),
            gameCatalog = item,
            isFavorite = item.id in favoriteIds,
            hasNewChaptersSoon = item.id in newChaptersSoonGameIds,
            onTap = { card -> onStoryTap(card) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.verticalStoriesSection(
    verticalStories: List<GameCatalog>,
    favoriteIds: Set<String>,
    onStoryTap: (GameCatalog) -> Unit
) {
    if (verticalStories.isEmpty()) return

    item(key = "vertical_stories") {
        GamePosterRow(
            modifier = Modifier.padding(vertical = 8.dp),
            stories = verticalStories,
            favoriteIds = favoriteIds,
            onTap = onStoryTap
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.releaseScheduleTitleSection(fullStories: List<GameCatalog>) {
    if (fullStories.isEmpty()) return

    item(key = "release_schedule_title") {
        Text(
            text = stringResource(R.string.sutoko_main_section_title_release_schedule),
            fontSize = 14.sp,
            style = SutokoTypography.body1.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = Color(0xFFFAFAFA)
            ),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.fullStoriesSection(
    fullStories: List<GameCatalog>,
    favoriteIds: Set<String>,
    newChaptersSoonGameIds: Set<String>,
    onStoryTap: (GameCatalog) -> Unit
) {
    if (fullStories.isEmpty()) return

    itemsIndexed(
        items = fullStories,
        key = { _, item -> "card_${item.id}" }
    ) { _, item ->
        GameCard(
            modifier = Modifier.animateItemPlacement(),
            gameCatalog = item,
            isFavorite = item.id in favoriteIds,
            hasNewChaptersSoon = item.id in newChaptersSoonGameIds,
            onTap = { card -> onStoryTap(card) }
        )
    }
}
