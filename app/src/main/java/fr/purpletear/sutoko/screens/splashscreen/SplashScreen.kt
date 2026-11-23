package fr.purpletear.sutoko.screens.splashscreen

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
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
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import fr.purpletear.sutoko.R

// Align the chime's accent with the bright logo at 2x video speed.
private const val SPLASH_SOUND_POSITION_MS = 2_600L

/**
 * Composable function for the splash screen.
 * Displays a loading indicator while fetching games.
 * Navigates to the main screen when animations are finished and data is loaded.
 *
 * @param onNavigateToMain Callback to navigate to the main screen when loading is complete.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashScreenViewModel = hiltViewModel(),
    shouldFinishSound: () -> Boolean = { true },
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
                    .fillMaxWidth(0.65f)
                    .widthIn(max = 360.dp),
                onAnimationFinished = {
                    viewModel.setAnimationsFinished()
                },
                shouldFinishSound = shouldFinishSound,
            )

            // Animate the alpha of the CircularProgressIndicator based on animation state
            val progressAlpha by animateFloatAsState(
                targetValue = if (areAnimationsFinished && !isReadyToNavigate) 1f else 0f,
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
@OptIn(UnstableApi::class)
internal fun VideoSplashScreen(
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit = {},
    shouldFinishSound: () -> Boolean = { true },
) {
    val context = LocalContext.current
    val activity = LocalActivity.current as? ComponentActivity

    val currentOnAnimationFinished by rememberUpdatedState(onAnimationFinished)
    val currentShouldFinishSound by rememberUpdatedState(shouldFinishSound)

    val exoPlayer = remember(context, activity) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = ExoPlayer.REPEAT_MODE_OFF

            // Keep the entire six-second reveal, but play it in about three seconds.
            setPlaybackSpeed(2f)
            volume = 0f
            val videoUri = rawResourceUri(context, R.raw.dark_sutoko_purpletear_splashcreen)
            setMediaItem(MediaItem.fromUri(videoUri))
        }
    }

    DisposableEffect(exoPlayer) {
        val splashSound = activity?.let(::SplashSound)
        var finished = false
        var videoEnded = false
        fun finish() {
            if (!finished) {
                finished = true
                currentOnAnimationFinished()
            }
        }

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    videoEnded = true
                    finish()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                // A decoder failure must not leave the user stuck on the splash screen.
                finish()
            }
        }
        exoPlayer.addListener(listener)
        val soundCue = exoPlayer.createMessage { _, _ ->
            if (!finished) splashSound?.play()
        }
            .setLooper(exoPlayer.applicationLooper)
            .setPosition(SPLASH_SOUND_POSITION_MS)
            .send()
        exoPlayer.prepare()
        exoPlayer.play()

        onDispose {
            soundCue.cancel()
            splashSound?.onVideoDisposed(finishPlaying = videoEnded && currentShouldFinishSound())
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier.aspectRatio(16f / 9f),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    // Set controller properties before assigning the player
                    useController = false

                    // Set controller visibility to GONE explicitly (null-safe: the
                    // controller view may be absent when useController = false)
                    findViewById<android.view.View>(androidx.media3.ui.R.id.exo_controller)?.visibility =
                        android.view.View.GONE

                    // Set player after controller settings
                    player = exoPlayer

                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

internal fun rawResourceUri(context: Context, @androidx.annotation.RawRes id: Int): Uri =
    Uri.parse("android.resource://${context.packageName}/$id")
