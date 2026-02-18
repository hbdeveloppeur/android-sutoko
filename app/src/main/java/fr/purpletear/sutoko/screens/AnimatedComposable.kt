package fr.purpletear.sutoko.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(
                    durationMillis = 760,
                    easing = FastOutSlowInEasing
                ),
                initialOffset = { fullSize -> fullSize / 4 }     // move the whole width in from the right
            ) + fadeIn(
                animationSpec = tween(760, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(
                    durationMillis = 760,
                    easing = FastOutSlowInEasing
                ),
                targetOffset = { fullSize -> fullSize / 4 }
            ) + fadeOut(
                animationSpec = tween(760, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(760, easing = FastOutSlowInEasing),
                initialOffset = { fullSize -> fullSize / 4 }
            ) + fadeIn(
                animationSpec = tween(760, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(760, easing = FastOutSlowInEasing),
                targetOffset = { fullSize -> fullSize / 4 }
            ) + fadeOut(
                animationSpec = tween(760, easing = FastOutSlowInEasing)
            )
        },
        content = content,
    )
}

