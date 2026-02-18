package fr.purpletear.sutoko.screens.main.presentation.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.imageLoader
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purpletear.ai_conversation.ui.navigation.AiConversationRouteDestination
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.game_presentation.components.GameCard
import com.purpletear.game_presentation.components.GameSquares
import fr.purpletear.sutoko.BuildConfig
import fr.purpletear.sutoko.screens.main.domain.popup.util.MainMenuCategory
import fr.purpletear.sutoko.screens.main.presentation.HomeScreenViewModel
import fr.purpletear.sutoko.screens.main.presentation.MainEvents
import fr.purpletear.sutoko.screens.main.presentation.MainScreenPages
import fr.purpletear.sutoko.screens.main.presentation.screens.TopNavigation
import fr.purpletear.sutoko.screens.main.presentation.screens.home.components.AiConversationCard
import fr.purpletear.sutoko.screens.main.presentation.screens.home.components.HeaderPager
import fr.purpletear.sutoko.screens.main.presentation.screens.home.components.Menu
import com.purpletear.game_presentation.util.ImmutableList as GameImmutableList
import com.purpletear.game_presentation.util.ImmutableMap as GameImmutableMap

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
    viewModel: HomeScreenViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberLazyListState()
    val systemUiController = rememberSystemUiController()

    // Collect navigation events with lifecycle awareness
    val navEvent by viewModel.navEvents.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(navEvent) {
        navEvent?.let { route ->
            mainNavController.navigate(route)
        }
    }

    // Preload first news image for better UX
    LaunchedEffect(viewModel.news.value) {
        viewModel.news.value.firstOrNull()?.let { firstNews ->
            val request = ImageRequest.Builder(context)
                .data(firstNews.media.filename)
                .build()
            context.imageLoader.enqueue(request)
        }
    }

    // System UI settings
    LaunchedEffect(Unit) {
        systemUiController.isStatusBarVisible = true
    }

    HomeContent(
        scrollState = scrollState,
        news = viewModel.news.value,
        squareStories = viewModel.squareStories.value,
        fullStories = viewModel.fullStories.value,
        squareIcons = viewModel.squareIcons.value,
        categoryState = viewModel.categoryState.value,
        coinsBalance = viewModel.coinsBalance.value,
        aiConversationMessageCount = viewModel.aiConversationMessageCount.value,
        displayAiConversationCard = viewModel.displayAiConversationCard.value,
        customerCoins = viewModel.customer.getCoins(),
        customerDiamonds = viewModel.customer.getDiamonds(),
        onAccountButtonPressed = { viewModel.onEvent(MainEvents.AccountButtonPressed) },
        onCoinsButtonPressed = { viewModel.onEvent(MainEvents.CoinButtonPressed) },
        onDiamondsButtonPressed = { viewModel.onEvent(MainEvents.DiamondButtonPressed) },
        onOptionsButtonPressed = { viewModel.onEvent(MainEvents.OptionButtonPressed) },
        onNewsPressed = { action -> viewModel.handleAppAction(action = action) },
        onCategorySelected = { category ->
            viewModel.onEvent(MainEvents.TapMenu(category))
        },
        onSquareStoryTap = { card ->
            viewModel.onEvent(MainEvents.Open(card))
            mainNavController.navigate(MainScreenPages.GamePreview.createRoute(card.id))
        },
        onFullStoryTap = { card ->
            viewModel.onEvent(MainEvents.Open(card))
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
    news: List<com.purpletear.sutoko.news.model.News>,
    squareStories: fr.purpletear.sutoko.presentation.util.ImmutableList<com.purpletear.sutoko.game.model.Game>,
    fullStories: fr.purpletear.sutoko.presentation.util.ImmutableList<com.purpletear.sutoko.game.model.Game>,
    squareIcons: fr.purpletear.sutoko.presentation.util.ImmutableMap<Int, Int?>,
    categoryState: MainMenuCategory,
    coinsBalance: Resource<com.purpletear.shop.domain.model.Balance>,
    aiConversationMessageCount: Int?,
    displayAiConversationCard: Boolean,
    customerCoins: Int,
    customerDiamonds: Int,
    onAccountButtonPressed: () -> Unit,
    onCoinsButtonPressed: () -> Unit,
    onDiamondsButtonPressed: () -> Unit,
    onOptionsButtonPressed: () -> Unit,
    onNewsPressed: (com.purpletear.sutoko.core.domain.appaction.AppAction) -> Unit,
    onCategorySelected: (MainMenuCategory) -> Unit,
    onSquareStoryTap: (com.purpletear.sutoko.game.model.Game) -> Unit,
    onFullStoryTap: (com.purpletear.sutoko.game.model.Game) -> Unit,
    onAiConversationTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = scrollState,
        modifier = modifier
            .navigationBarsPadding()
            .systemBarsPadding()
            .padding(bottom = 55.dp)
    ) {
        topNavigationSection(
            coins = coinsBalance.data?.coins ?: customerCoins,
            diamonds = coinsBalance.data?.diamonds ?: customerDiamonds,
            isLoading = coinsBalance is Resource.Loading,
            onAccountButtonPressed = onAccountButtonPressed,
            onCoinsButtonPressed = onCoinsButtonPressed,
            onDiamondsButtonPressed = onDiamondsButtonPressed,
            onOptionsButtonPressed = onOptionsButtonPressed
        )

        newsSection(
            news = news,
            onNewsPressed = onNewsPressed
        )

        categoryMenuSection(
            currentCategory = categoryState,
            onCategorySelected = onCategorySelected
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
            categoryState = categoryState,
            aiConversationMessageCount = aiConversationMessageCount,
            displayAiConversationCard = displayAiConversationCard,
            onAiConversationTap = onAiConversationTap
        )

        fullStoriesSection(
            fullStories = fullStories,
            onStoryTap = onFullStoryTap
        )
    }
}

private fun LazyListScope.topNavigationSection(
    coins: Int,
    diamonds: Int,
    isLoading: Boolean,
    onAccountButtonPressed: () -> Unit,
    onCoinsButtonPressed: () -> Unit,
    onDiamondsButtonPressed: () -> Unit,
    onOptionsButtonPressed: () -> Unit
) {
    item(key = "top_navigation") {
        TopNavigation(
            coins = coins,
            diamonds = diamonds,
            isLoading = isLoading,
            onAccountButtonPressed = onAccountButtonPressed,
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

private fun LazyListScope.categoryMenuSection(
    currentCategory: MainMenuCategory,
    onCategorySelected: (MainMenuCategory) -> Unit
) {
    item(key = "category_menu") {
        Menu(
            currentCategory = currentCategory,
            onTap = onCategorySelected
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.squareStoriesSection(
    squareStories: fr.purpletear.sutoko.presentation.util.ImmutableList<com.purpletear.sutoko.game.model.Game>,
    fullStories: fr.purpletear.sutoko.presentation.util.ImmutableList<com.purpletear.sutoko.game.model.Game>,
    squareIcons: fr.purpletear.sutoko.presentation.util.ImmutableMap<Int, Int?>,
    onStoryTap: (com.purpletear.sutoko.game.model.Game) -> Unit
) {
    if (squareStories.items.isEmpty() || fullStories.items.isEmpty()) return

    item(key = "square_stories") {
        GameSquares(
            stories = GameImmutableList(squareStories.items),
            icons = GameImmutableMap(squareIcons.map),
            onTap = onStoryTap
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.squareStoriesAsCardsSection(
    squareStories: fr.purpletear.sutoko.presentation.util.ImmutableList<com.purpletear.sutoko.game.model.Game>,
    fullStories: fr.purpletear.sutoko.presentation.util.ImmutableList<com.purpletear.sutoko.game.model.Game>,
    onStoryTap: (com.purpletear.sutoko.game.model.Game) -> Unit
) {
    if (squareStories.items.isEmpty() || fullStories.items.isNotEmpty()) return

    itemsIndexed(
        items = squareStories.items,
        key = { _, item -> "card_${item.id}" }
    ) { _, item ->
        GameCard(
            modifier = Modifier.animateItemPlacement(),
            game = item,
            onTap = { card -> onStoryTap(card) }
        )
    }
}

private fun LazyListScope.aiConversationSection(
    categoryState: MainMenuCategory,
    aiConversationMessageCount: Int?,
    displayAiConversationCard: Boolean,
    onAiConversationTap: () -> Unit
) {
    if (categoryState !== MainMenuCategory.All) return

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
    fullStories: fr.purpletear.sutoko.presentation.util.ImmutableList<com.purpletear.sutoko.game.model.Game>,
    onStoryTap: (com.purpletear.sutoko.game.model.Game) -> Unit
) {
    if (fullStories.items.isEmpty()) return

    itemsIndexed(
        items = fullStories.items,
        key = { _, item -> "card_${item.id}" }
    ) { _, item ->
        GameCard(
            modifier = Modifier.animateItemPlacement(),
            game = item,
            onTap = { card -> onStoryTap(card) }
        )
    }
}
