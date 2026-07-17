package fr.purpletear.sutoko.screens.main.presentation.screens.home

import android.util.Log
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purpletear.aiconversation.presentation.navigation.AiConversationRouteDestination
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.game.presentation.game_catalog.GameCard
import com.purpletear.game.presentation.game_catalog.GameSquares
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.news.model.News
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import fr.purpletear.sutoko.BuildConfig
import fr.purpletear.sutoko.screens.main.presentation.HomeScreenViewModel
import fr.purpletear.sutoko.screens.main.presentation.MainEvents
import fr.purpletear.sutoko.screens.main.presentation.MainScreenPages
import fr.purpletear.sutoko.screens.main.presentation.screens.TopNavigation
import fr.purpletear.sutoko.screens.main.presentation.screens.home.components.AiConversationCard
import fr.purpletear.sutoko.screens.main.presentation.screens.home.components.HeaderPager
import kotlinx.coroutines.CancellationException

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
    val news = viewModel.news.collectAsStateWithLifecycle()

    // Collect navigation events with lifecycle awareness
    val navEvent by viewModel.navEvents.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(navEvent) {
        navEvent?.let { route ->
            try {
                mainNavController.navigate(route)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error navigating to route: $route", e)
            }
        }
    }

    // System UI settings
    LaunchedEffect(Unit) {
        systemUiController.isStatusBarVisible = true
    }

    val balance = viewModel.balance.collectAsStateWithLifecycle()
    val isConnected = viewModel.isConnected.collectAsStateWithLifecycle()

    HomeContent(
        scrollState = scrollState,
        news = news.value,
        squareStories = viewModel.squareStories.value,
        fullStories = viewModel.fullStories.value,
        squareIcons = viewModel.squareIcons.value,
        coinsBalance = balance.value,
        isConnected = isConnected.value,
        aiConversationMessageCount = viewModel.aiConversationMessageCount.value,
        displayAiConversationCard = viewModel.displayAiConversationCard.value,
        onAccountButtonPressed = onAccountPressed,
        onSignInButtonPressed = onSignInPressed,
        onCoinsButtonPressed = onCoinsPressed,
        onDiamondsButtonPressed = onDiamondsPressed,
        onOptionsButtonPressed = onOptionsPressed,
        onNewsPressed = { action -> viewModel.handleAppAction(action = action) },
        onSquareStoryTap = { card ->
            mainNavController.navigate(MainScreenPages.GamePreview.createRoute(card.id))
        },
        onFullStoryTap = { card ->
            mainNavController.navigate(MainScreenPages.GamePreview.createRoute(card.id))
        },
        onAiConversationTap = {
            if (viewModel.displayAiConversationCard.value || BuildConfig.DEBUG) {
                mainNavController.navigate(AiConversationRouteDestination.Home.destination)
            } else {
                viewModel.onEvent(MainEvents.TapAiConversationMenu)
            }
        }
    )
}

/**
 * Stateless HomeContent composable for better testability and preview support.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeContent(
    scrollState: LazyListState,
    news: List<News>,
    squareStories: List<GameCatalog>,
    fullStories: List<GameCatalog>,
    squareIcons: Map<Int, Int?>,
    coinsBalance: Resource<Balance>,
    isConnected: Boolean,
    aiConversationMessageCount: Int?,
    displayAiConversationCard: Boolean,
    onAccountButtonPressed: () -> Unit,
    onSignInButtonPressed: () -> Unit,
    onCoinsButtonPressed: () -> Unit,
    onDiamondsButtonPressed: () -> Unit,
    onOptionsButtonPressed: () -> Unit,
    onNewsPressed: (com.purpletear.sutoko.core.domain.appaction.AppAction) -> Unit,
    onSquareStoryTap: (GameCatalog) -> Unit,
    onFullStoryTap: (GameCatalog) -> Unit,
    onAiConversationTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = scrollState,
        modifier = modifier
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

        newsSection(
            news = news,
            onNewsPressed = onNewsPressed
        )

        squareStoriesSection(
            squareStories = squareStories,
            fullStories = fullStories,
            squareIcons = squareIcons,
            onStoryTap = onSquareStoryTap
        )

        squareStoriesAsCardsSection(
            squareStories = squareStories,
            fullStories = fullStories,
            onStoryTap = onFullStoryTap
        )

        aiConversationSection(
            aiConversationMessageCount = aiConversationMessageCount,
            displayAiConversationCard = displayAiConversationCard,
            onAiConversationTap = onAiConversationTap
        )

        fullStoriesSection(
            fullStories = fullStories,
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

private fun LazyListScope.newsSection(
    news: List<com.purpletear.sutoko.news.model.News>,
    onNewsPressed: (com.purpletear.sutoko.core.domain.appaction.AppAction) -> Unit
) {
    if (news.isEmpty()) return

    item(key = "news_pager") {
        val initialPageState = rememberSaveable { mutableIntStateOf(0) }

        HeaderPager(
            news = news,
            initialPage = initialPageState.intValue,
            onNewsPressed = onNewsPressed,
            onPageChanged = { initialPageState.intValue = it }
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
            onTap = { card -> onStoryTap(card) }
        )
    }
}

private fun LazyListScope.aiConversationSection(
    aiConversationMessageCount: Int?,
    displayAiConversationCard: Boolean,
    onAiConversationTap: () -> Unit
) {
    item(key = "ai_conversation") {
        AiConversationCard(
            messagesCount = aiConversationMessageCount,
            onTap = onAiConversationTap,
            isAvailable = displayAiConversationCard
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.fullStoriesSection(
    fullStories: List<GameCatalog>,
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
            onTap = { card -> onStoryTap(card) }
        )
    }
}
