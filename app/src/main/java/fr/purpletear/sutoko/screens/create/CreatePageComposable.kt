package fr.purpletear.sutoko.screens.create

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.purpletear.core.presentation.util.openAppInStore
import com.purpletear.game.presentation.game_catalog.GameCardCompact
import com.purpletear.game.presentation.model.GameItem
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.create.components.create_story_button.CreateStoryButton
import fr.purpletear.sutoko.screens.create.components.create_story_button.CreateStoryButtonVariant
import fr.purpletear.sutoko.screens.create.components.load_more_button.LoadMoreButton
import fr.purpletear.sutoko.screens.create.components.search_box.SearchBox
import fr.purpletear.sutoko.screens.create.components.section_title.SectionTitle
import fr.purpletear.sutoko.screens.main.presentation.screens.TopNavigation

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun CreatePageComposable(
    modifier: Modifier = Modifier,
    viewModel: CreateViewModel = hiltViewModel(),
    onAccountPressed: () -> Unit = {},
    onOptionsPressed: () -> Unit = {},
    onCoinsPressed: () -> Unit = {},
    onDiamondsPressed: () -> Unit = {},
    openGame: (GameItem) -> Unit = {},
) {
    val isRefreshing by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val balance = viewModel.balance.collectAsStateWithLifecycle()
    val games = viewModel.games.collectAsStateWithLifecycle()
    val appBuildNumber = viewModel.appBuildNumber

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CreatePageEvent.OpenAppStore -> context.openAppInStore()
            }
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(top = 12.dp)
                    .padding(bottom = 70.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    },
            ) {
                item {
                    TopNavigation(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(start = 8.dp),
                        balance = balance.value,
                        onAccountButtonPressed = onAccountPressed,
                        onCoinsButtonPressed = onCoinsPressed,
                        onDiamondsButtonPressed = onDiamondsPressed,
                        onOptionsButtonPressed = onOptionsPressed,
                    )
                }

                item {
                    SectionTitle(
                        text = stringResource(R.string.create_page_section_title_community),
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                    )
                }

                item {
                    CreateStoryButton(
                        text = stringResource(R.string.create_page_button_create_story),
                        variant = CreateStoryButtonVariant.Violet,
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp)
                    )
                }

                item {
                    SearchBox(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        onSearch = { query ->
                            viewModel.onSearchSubmit(query)
                        },
                        onValueChange = { query ->
                            viewModel.onSearchQueryChange(query)
                        }
                    )
                }

                items(
                    count = games.value.size
                ) { index ->
                    val game = games.value[index]
                    GameCardCompact(
                        modifier = Modifier.padding(top = 16.dp),
                        isPending = false,
                        isPurchasing = false,
                        isPurchaseLoading = false,
                        currentChapter = null,
                        appBuildNumber = appBuildNumber,
                        isGameFinished = false,
                        game = game,
                        showGetButton = true,
                        onGetClick = { viewModel.onGameGetClick(game) },
                        onOpenClick = { openGame(game) },
                        onCancelClick = { viewModel.onGameCancelClick(game.id) }
                    )
                }


                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        LoadMoreButton(
                            // TODO
                            onClick = { },
                            isLoading = false,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp)
                        )
                    }
                }
                // Bottom spacer for better scrolling experience
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color(0xFF2D2D2D),
                contentColor = Color.White
            )

            /*GamePreviewModal(
                isVisible = isPreviewModalVisible,
                onDismiss = {
                    Log.d("CreatePageComposable", "onDismiss called - hiding modal")
                    isPreviewModalVisible = false
                    selectedGameId = null
                },
                gameId = selectedGameId,
                onPlayGame = { game ->
                    Log.d(
                        "CreatePageComposable",
                        "onPlayGame called with game=${game.id}"
                    )
                    onGamePressed(game)
                },
                onGameDeleted = {
                    Log.d(
                        "CreatePageComposable",
                        "onGameDeleted called - refreshing games list"
                    )
                }
            ) */
        }
    }
}

