package com.purpletear.ai_conversation.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.purpletear.ai_conversation.domain.enums.Visibility
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.component.badge.BadgeComposable
import com.purpletear.ai_conversation.ui.component.divider.DividerComposable
import com.purpletear.ai_conversation.ui.component.image_action_card.ImageActionCardComposable
import com.purpletear.ai_conversation.ui.component.title.Title
import com.purpletear.ai_conversation.ui.screens.home.components.button.HomePlayButtonsComposable
import com.purpletear.ai_conversation.ui.screens.home.components.characters_slider.CharacterSlider
import com.purpletear.ai_conversation.ui.screens.home.components.characters_table.CharactersTableComposable
import com.purpletear.ai_conversation.ui.screens.home.components.header.MainHeaderComposable
import com.purpletear.ai_conversation.ui.screens.home.viewModels.AiConversationHomeViewModel
import com.purpletear.ai_conversation.ui.theme.BlueBackground
import com.purpletear.core.presentation.services.performVibration


@Composable
fun AiConversationHomeScreen(
    navController: NavHostController,
    viewModel: AiConversationHomeViewModel,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                viewModel.onResume()
            }

            else -> {}
        }
    }

    // Show header only after the navigation enter animation has finished (760ms)
    var showHeader by remember { mutableStateOf(false) }
    // Enter: delay to show header after navigation animation
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(920)
        showHeader = true
    }
    // Exit: hide header as soon as screen is not RESUMED (start of exit transition)
    LaunchedEffect(lifecycleState) {
        if (lifecycleState != Lifecycle.State.RESUMED) {
            showHeader = false
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {
        Header(viewModel = viewModel, showVideo = showHeader)

        val listState = rememberLazyListState()
        var backgroundAlpha by remember { mutableFloatStateOf(0f) }
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp

        val density = LocalDensity.current
        val max = with(density) { screenWidth.toPx() }


        Box(
            Modifier
                .fillMaxSize()
                .alpha(backgroundAlpha)
                .background(BlueBackground)
        )

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemScrollOffset }
                .collect { offset ->
                    if (listState.layoutInfo.visibleItemsInfo.isNotEmpty() && listState.layoutInfo.visibleItemsInfo[0].index == 0) {
                        backgroundAlpha = 1f - (max - offset).coerceIn(0f, max) / max
                    }
                }
        }

        List(
            Modifier.fillMaxWidth(1f),
            state = listState,
            contentPaddingValues = PaddingValues(bottom = 62.dp),
            navHostController = navController,
            viewModel = viewModel
        )
    }
}


@Composable
private fun Header(
    modifier: Modifier = Modifier,
    viewModel: AiConversationHomeViewModel,
    showVideo: Boolean,
) {
    val colors = listOf(
        Color(0xFF01050A).copy(1f),
        Color(0xFF302D4C).copy(0f),
    )
    Column(modifier) {
        MainHeaderComposable(viewModel = viewModel, showVideo = showVideo)

        Box(
            Modifier
                .fillMaxWidth()
                .height(600.dp)
                .background(brush = Brush.verticalGradient(colors))
        )
    }
}

@Composable
private fun List(
    modifier: Modifier = Modifier,
    state: LazyListState,
    contentPaddingValues: PaddingValues,
    navHostController: NavHostController,
    viewModel: AiConversationHomeViewModel,
) {
    val context = LocalContext.current

    LazyColumn(
        state = state,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = contentPaddingValues
    ) {

        item(key = "FirstItemSpacer") {
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(LocalConfiguration.current.screenWidthDp.dp)
            )
        }

        item(key = "Title") {
            Title(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),

                title = stringResource(R.string.ai_conversation_sutoko_home_title),
                subtitle = stringResource(R.string.ai_conversation_home_subtitle),
                style = MaterialTheme.typography.titleLarge,
            )
        }


        item(key = "CharacterGrid") {
            CharacterSlider(
                Modifier.padding(end = 20.dp),
                elementSize = 60.dp,
                items = viewModel.gridItems.value,
                isEnabled = true,
                onClickNewElement = {
                    performVibration(context)
                    navHostController.navigate("add_character")
                },
                onClickElement = {
                    performVibration(context)
                    viewModel.onClickElement(it)
                }
            )
        }

        item(key = "Buttons") {
            HomePlayButtonsComposable(
                navHostController = navHostController,
                viewModel = viewModel,
            )
        }

        item(key = "ImageAction1") {
            ImageActionCardComposable(
                Modifier
                    .fillMaxWidth(0.92f)
                    .padding(top = 12.dp),
                title = stringResource(R.string.ai_conversation_home_image_action_title),
                url = "https://data.sutoko.app/resources/sutoko-ai/image/coins_snow_holo.jpg",
                onClick = {
                    viewModel.openMessagesCoinsDialog()
                }
            )
        }

        item(key = "Divider2") {
            DividerComposable(Modifier.padding(vertical = 12.dp))
        }

        item(key = "Title-2") {
            Title(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                title = stringResource(R.string.ai_conversation_title_create_character),
                subtitle = stringResource(R.string.ai_conversation_subtitle_create_character),
                style = MaterialTheme.typography.titleMedium
            )
        }

        item(key = "Badges-1") {
            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth(0.92f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BadgeComposable(text = "Realistic")
                BadgeComposable(text = "K-Drama")
                BadgeComposable(text = "Manga")
            }
        }

        item(key = "characters-grid") {
            CharactersTableComposable(
                characters = viewModel.characters.value.filter { it.visibility == Visibility.Private },
                navHostController = navHostController,
            )
        }

        item(key = "Divider3") {
            DividerComposable(Modifier.padding(vertical = 12.dp))
        }

        item(key = "ImageAction2") {
            ImageActionCardComposable(
                Modifier.fillMaxWidth(0.92f),
                title = stringResource(R.string.ai_conversation_cta_join_discord),
                url = "https://data.sutoko.app/resources/sutoko-ai/image/poster_discord.jpg",
                onClick = {
                    viewModel.openDiscord()
                }
            )
        }
    }
}
