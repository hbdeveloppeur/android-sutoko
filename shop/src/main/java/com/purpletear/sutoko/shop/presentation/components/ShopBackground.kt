package com.purpletear.sutoko.shop.presentation.components

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.purpletear.sutoko.shop.R

private const val SHOP_BACKGROUND_VIDEO_URL =
    "https://data.sutoko.app/resources/shop_background.mp4"

/**
 * Full-screen shop background: static image, looping video on top of it,
 * and a dark scrim to keep foreground content readable.
 */
@Composable
fun ShopBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF9D3C4D))
    ) {
        AsyncImage(
            model = R.drawable.shop_background_fix,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        LoopingVideo(
            url = SHOP_BACKGROUND_VIDEO_URL,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
        )
    }
}

/**
 * Looping, crop-scaled video. Playback errors are swallowed: the static image
 * underneath remains as a graceful fallback.
 */
@Composable
private fun LoopingVideo(url: String, modifier: Modifier = Modifier) {
    var videoView by remember { mutableStateOf<VideoView?>(null) }

    AndroidView(
        factory = { context ->
            val frameLayout = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
            val newVideoView = VideoView(context).apply {
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                    gravity = Gravity.CENTER
                }
                setVideoURI(url.toUri())
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    val videoWidth = mediaPlayer.videoWidth
                    val videoHeight = mediaPlayer.videoHeight
                    if (videoWidth > 0 && videoHeight > 0) {
                        post {
                            val parentWidth = (parent as FrameLayout).width
                            val parentHeight = (parent as FrameLayout).height
                            if (parentWidth > 0 && parentHeight > 0) {
                                val scale = maxOf(
                                    parentWidth.toFloat() / videoWidth.toFloat(),
                                    parentHeight.toFloat() / videoHeight.toFloat(),
                                )
                                layoutParams = FrameLayout.LayoutParams(
                                    (videoWidth * scale).toInt(),
                                    (videoHeight * scale).toInt(),
                                ).apply { gravity = Gravity.CENTER }
                            }
                        }
                    }
                    start()
                }
                setOnErrorListener { _, _, _ -> true }
            }
            frameLayout.addView(newVideoView)
            videoView = newVideoView
            frameLayout
        },
        modifier = modifier,
    )

    DisposableEffect(Unit) {
        onDispose {
            videoView?.let {
                if (it.isPlaying) it.stopPlayback()
                it.suspend()
            }
            videoView = null
        }
    }
}
