package fr.purpletear.sutoko.screens.splashscreen

import android.content.Context
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import fr.purpletear.sutoko.R

/**
 * Composable function for the splash screen.
 * Displays a loading indicator while fetching news and games.
 * Navigates to the main screen when animations are finished and data is loaded.
 *
 * @param onNavigateToMain Callback to navigate to the main screen when loading is complete.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashScreenViewModel = hiltViewModel()
) {
    // Collect the states
    val isReadyToNavigate by viewModel.isReadyToNavigate.collectAsState()
    val areAnimationsFinished by viewModel.areAnimationsFinished.collectAsState()

    // Get the SystemUiController to hide/show the status bar
    val systemUiController = rememberSystemUiController()

    // Hide status bar when entering the splash screen
    LaunchedEffect(Unit) {
        systemUiController.isStatusBarVisible = false
    }

    // Restore status bar when leaving the splash screen
    DisposableEffect(Unit) {
        onDispose {
            systemUiController.isStatusBarVisible = true
        }
    }

    // Navigate to main screen when ready
    LaunchedEffect(isReadyToNavigate) {
        if (isReadyToNavigate) {
            onNavigateToMain()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF000000)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            VideoSplashScreen(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .widthIn(max = 400.dp),
                onAnimationFinished = {
                    viewModel.setAnimationsFinished()
                }
            )

            // Animate the alpha of the CircularProgressIndicator based on animation state
            val progressAlpha by animateFloatAsState(
                targetValue = if (areAnimationsFinished) 1f else 0f,
                animationSpec = tween(durationMillis = 280),
                label = "ProgressIndicatorAlpha"
            )

            CircularProgressIndicator(
                modifier = Modifier
                    .size(16.dp)
                    .alpha(progressAlpha)
                    .graphicsLayer(alpha = progressAlpha),
                color = Color.Gray,
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun VideoSplashScreen(
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit = {}
) {
    val context = LocalContext.current

    // SAFETY: Keep player in rememberUpdatedState to ensure one instance per recomposition
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = ExoPlayer.REPEAT_MODE_OFF

            playWhenReady = false
            volume = 0f // No sound
            val videoUri = rawResVideoUri(context, R.raw.dark_sutoko_purpletear_splashcreen)
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }

    LaunchedEffect(exoPlayer) {
        kotlinx.coroutines.delay(1_000) // 1 second
        exoPlayer.playWhenReady = true

        // The animation takes 5 seconds to complete as per requirements
        kotlinx.coroutines.delay(5_000) // 5 seconds
        onAnimationFinished()
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release() // Memory-leak free!
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                // Set controller properties before assigning the player
                useController = false

                // Set controller visibility to GONE explicitly
                findViewById<android.view.View>(androidx.media3.ui.R.id.exo_controller).visibility =
                    android.view.View.GONE

                // Set player after controller settings
                player = exoPlayer

                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier
            .aspectRatio(16f / 9f)
            .then(modifier)
    )
}

/**
 * Helper to get a URI for a raw resource video.
 */
fun rawResVideoUri(context: Context, @androidx.annotation.RawRes id: Int): Uri =
    Uri.parse("android.resource://${context.packageName}/$id")
