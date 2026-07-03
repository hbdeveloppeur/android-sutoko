package fr.purpletear.sutoko.screens.main.presentation.screens

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.create.CreatePageComposable
import fr.purpletear.sutoko.screens.main.presentation.HomeScreenViewModel
import fr.purpletear.sutoko.screens.main.presentation.MainEvents
import fr.purpletear.sutoko.screens.main.presentation.screens.components.navigation.BottomNavItem
import fr.purpletear.sutoko.screens.main.presentation.screens.home.HomeScreen

@Composable
fun MainScreen(
    viewModel: HomeScreenViewModel,
    mainNavController: NavController,
    onAccountPressed: () -> Unit,
    onOptionsPressed: () -> Unit,
    onCoinsPressed: () -> Unit,
    onDiamondsPressed: () -> Unit,
    onGamePressed: (String, Boolean) -> Unit,
    onCreateStoryPressed: () -> Unit,
    size: WindowWidthSizeClass
) {

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colors.background
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

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(if (size != WindowWidthSizeClass.Compact) .5f else 1f)
        ) {
            Background()


            val bottomNavigationController = rememberNavController()

            NavHost(
                navController = bottomNavigationController,
                startDestination = BottomNavItem.Home.route,
                modifier = Modifier
                    .fillMaxHeight()
                    .sizeIn(maxWidth = 500.dp)
                    .navigationBarsPadding()
                    .padding(bottom = BottomNavigationHeight)
                    .align(Alignment.Center)
            ) {
                composable(
                    route = BottomNavItem.Home.route,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None }
                ) {
                    HomeScreen(
                        mainNavController = mainNavController,
                        onAccountPressed = onAccountPressed,
                        onOptionsPressed = onOptionsPressed,
                        onCoinsPressed = onCoinsPressed,
                        onDiamondsPressed = onDiamondsPressed,
                        viewModel = viewModel
                    )
                }

                composable(
                    route = BottomNavItem.Create.route,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None }
                ) {
                    CreatePageComposable(
                        onAccountPressed = onAccountPressed,
                        onOptionsPressed = onOptionsPressed,
                        onCoinsPressed = onCoinsPressed,
                        onDiamondsPressed = onDiamondsPressed,
                        onCreateStoryPressed = onCreateStoryPressed,
                        openGame = { game, isLiveUpdateMode ->
                            onGamePressed(game.id, isLiveUpdateMode)
                        }
                    )
                }
            }

            BottomNavigation(
                navController = bottomNavigationController,
                onShopPressed = {
                    viewModel.onEvent(MainEvents.TapShop)
                }
            )

        }

    }
}


@Composable
private fun Background() {
    Box(
        Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Image(
            painterResource(R.drawable.book_details_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.6f)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4DB9EC).copy(0.16f),
                            Color(0xFF4DB9EC).copy(0.00001111f),
                        )
                    )
                )
        )
    }
}