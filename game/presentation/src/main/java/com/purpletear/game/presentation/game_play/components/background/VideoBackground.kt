package com.purpletear.game.presentation.game_play.components.background

import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import java.io.File

/**
 * Displays a video background using AndroidView with VideoView.
 * Video loops indefinitely and scales to fill the container (ContentScale.Crop behavior).
 * Video appears only when fully prepared for smooth transitions.
 *
 * @param videoPath The absolute path to the video file
 * @param modifier The modifier to be applied to the component
 */
@Composable
fun VideoBackground(
    videoPath: String,
    modifier: Modifier = Modifier
) {
    var isReady by remember(videoPath) { mutableStateOf(false) }
    var videoView by remember { mutableStateOf<VideoView?>(null) }

    Box(
        modifier = modifier.background(Color.Black)
    ) {
        if (isReady) {
            AndroidView(
                factory = { ctx ->
                    val frameLayout = FrameLayout(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    }

                    val newVideoView = VideoView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                            gravity = Gravity.CENTER
                        }

                        setVideoURI(File(videoPath).toUri())

                        setOnPreparedListener { mediaPlayer ->
                            mediaPlayer.isLooping = true
                            mediaPlayer.setVolume(0f, 0f)

                            val videoWidth = mediaPlayer.videoWidth
                            val videoHeight = mediaPlayer.videoHeight

                            if (videoWidth > 0 && videoHeight > 0) {
                                post {
                                    val parentWidth = (parent as FrameLayout).width
                                    val parentHeight = (parent as FrameLayout).height

                                    if (parentWidth > 0 && parentHeight > 0) {
                                        val scaleX = parentWidth.toFloat() / videoWidth.toFloat()
                                        val scaleY = parentHeight.toFloat() / videoHeight.toFloat()
                                        val scale = maxOf(scaleX, scaleY)

                                        val scaledWidth = (videoWidth * scale).toInt()
                                        val scaledHeight = (videoHeight * scale).toInt()

                                        layoutParams =
                                            FrameLayout.LayoutParams(scaledWidth, scaledHeight)
                                                .apply { gravity = Gravity.CENTER }
                                    }

                                    start()
                                }
                            } else {
                                start()
                            }
                        }

                        setOnErrorListener { _, _, _ -> true }
                    }

                    frameLayout.addView(newVideoView)
                    videoView = newVideoView
                    frameLayout
                },
                modifier = Modifier.fillMaxSize(),
                update = { frameLayout ->
                    val v = frameLayout.getChildAt(0) as? VideoView
                    if (v?.isPlaying == false) {
                        v.start()
                    }
                }
            )
        }

        DisposableEffect(videoPath) {
            isReady = true
            onDispose {
                videoView?.let {
                    if (it.isPlaying) {
                        it.stopPlayback()
                    }
                    it.suspend()
                }
                videoView = null
                isReady = false
            }
        }
    }
}

/**
 * Preview for VideoBackground that simulates video path changes.
 * Note: In Android Studio Preview, VideoView cannot actually play video.
 * This preview demonstrates the component structure and state behavior.
 */
@Preview(name = "VideoBackground - Initial Load", showBackground = true)
@Composable
private fun VideoBackgroundPreview() {
    // In preview, we can't use real video files, so we show the black placeholder state
    // This demonstrates how the component looks when not ready (before video loads)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        // Use a non-existent path to simulate "not ready" state visually
        // In preview, VideoView won't render, so we see black box
        VideoBackground(
            videoPath = "/data/user/0/fr.purpletear.sutoko/files/games/taHQ3oyAtC3/assets/121717e8-6672-4218-a288-6fcdcdaef952.mp4",
            modifier = Modifier.fillMaxSize()
        )

        // Overlay to indicate this is preview mode
        androidx.compose.material3.Text(
            text = "Preview: Black box = video loading state\n(VideoView doesn't render in Studio Preview)",
            color = Color.White,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.Center)
                .background(Color.Black.copy(alpha = 0.7f))
        )
    }
}

/**
 * Interactive preview that simulates video path changes.
 * Use this to verify the isReady state resets correctly when path changes.
 */
@Preview(name = "VideoBackground - Path Change Simulation", showBackground = true)
@Composable
private fun VideoBackgroundPathChangePreview() {
    val context = LocalContext.current
    val videoPaths = remember {
        listOf(
            "/data/user/0/fr.purpletear.sutoko/files/games/taHQ3oyAtC3/assets/video1.mp4",
            "/data/user/0/fr.purpletear.sutoko/files/games/taHQ3oyAtC3/assets/video2.mp4",
            "/data/user/0/fr.purpletear.sutoko/files/games/taHQ3oyAtC3/assets/video3.mp4"
        )
    }
    var currentIndex by remember { mutableStateOf(0) }
    val currentPath = videoPaths[currentIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        VideoBackground(
            videoPath = currentPath,
            modifier = Modifier.fillMaxSize()
        )

        // Controls to simulate video changes
        androidx.compose.material3.Surface(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.8f)),
            color = Color.Black.copy(alpha = 0.8f)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Text(
                    text = "Current: ${currentPath.substringAfterLast("/")}",
                    color = Color.White
                )
                androidx.compose.material3.Button(
                    onClick = { currentIndex = (currentIndex + 1) % videoPaths.size },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    androidx.compose.material3.Text("Change Video Path")
                }
            }
        }
    }
}
