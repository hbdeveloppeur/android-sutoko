package com.purpletear.game.presentation.game_preview

import android.media.MediaPlayer
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri

/**
 * A background media component that displays a video
 * The component plays a video that loops
 *
 * @param videoUrl URL of the video to play.
 * @param onFirstFrame Called once when the first video frame is rendered.
 */
@Composable
internal fun BackgroundMedia(
    videoUrl: String,
    modifier: Modifier = Modifier,
    onFirstFrame: () -> Unit = {},
) {
    // Remember the VideoView reference
    var videoView by remember { mutableStateOf<VideoView?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // Video player
        AndroidView(
            factory = { ctx ->
                // Create a FrameLayout to hold the VideoView
                val frameLayout = FrameLayout(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                }

                // Create a VideoView programmatically with a custom layout approach to match ContentScale.Crop
                val newVideoView = VideoView(ctx).apply {
                    // Initially set to MATCH_PARENT to fill the container
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                        gravity = Gravity.CENTER
                    }

                    setVideoURI(videoUrl.toUri())
                    setOnPreparedListener { mediaPlayer ->
                        // Set looping
                        mediaPlayer.isLooping = true

                        // Mute the video
                        mediaPlayer.setVolume(0f, 0f)

                        // Get video dimensions
                        val videoWidth = mediaPlayer.videoWidth
                        val videoHeight = mediaPlayer.videoHeight

                        if (videoWidth > 0 && videoHeight > 0) {
                            // Get the parent frame layout dimensions
                            post {
                                val parentWidth = (parent as FrameLayout).width
                                val parentHeight = (parent as FrameLayout).height

                                if (parentWidth > 0 && parentHeight > 0) {
                                    // Calculate the scaling factors
                                    val scaleX = parentWidth.toFloat() / videoWidth.toFloat()
                                    val scaleY = parentHeight.toFloat() / videoHeight.toFloat()

                                    // Use the larger scale to ensure the video fills the container (similar to ContentScale.Crop)
                                    val scale = maxOf(scaleX, scaleY)

                                    // Calculate new dimensions that maintain aspect ratio while filling the container
                                    val scaledWidth = (videoWidth * scale).toInt()
                                    val scaledHeight = (videoHeight * scale).toInt()

                                    // Update layout params to maintain aspect ratio while filling the container
                                    layoutParams =
                                        FrameLayout.LayoutParams(scaledWidth, scaledHeight)
                                            .apply {
                                                gravity = Gravity.CENTER
                                            }
                                }
                            }
                        }

                        // Start playing
                        start()
                    }
                    // Notify only when the first frame is actually rendered,
                    // so the caller can crossfade without a black gap.
                    setOnInfoListener { _, what, _ ->
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            onFirstFrame()
                        }
                        false
                    }
                    setOnErrorListener { _, _, _ ->
                        true
                    }
                }

                // Add VideoView to FrameLayout and store reference
                frameLayout.addView(newVideoView)
                videoView = newVideoView
                frameLayout
            },
            modifier = Modifier.fillMaxSize()
        )

        // Clean up video resources when the composable leaves composition
        DisposableEffect(Unit) {
            onDispose {
                // Clean up video resources
                videoView?.let {
                    if (it.isPlaying) {
                        it.stopPlayback()
                    }
                    it.suspend()
                }
                videoView = null
            }
        }
    }
}
