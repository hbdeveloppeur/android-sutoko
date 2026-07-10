package com.purpletear.game.presentation.game_play

import android.media.MediaPlayer
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purpletear.sutoko.game.model.chapter.IntroAlignment
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.model.scene.Scene
import kotlinx.coroutines.delay

private const val CINEMATIC_FADE_MS = 1000
private const val TAG = "CinematicScreen"

/**
 * Full-screen, non-interactive cinematic player.
 *
 * Walks the extracted linear [body] in order:
 *  - `Node.Scene`  → sets the background (reused `SceneComposable`) and advances immediately.
 *  - `Node.Sound`  → plays ambient audio on a dedicated player and advances immediately.
 *  - `Node.IntroSentence` → fades the line in, holds it, fades it out, then advances.
 *
 * When the body is exhausted, [onFinished] is invoked exactly once so the caller can resume the SMS
 * engine and pop back to the conversation.
 */
@Composable
internal fun CinematicScreen(
    body: List<Node>,
    loadScene: suspend (Int) -> Scene?,
    onFinished: () -> Unit,
) {
    var index by remember(body) { mutableIntStateOf(0) }
    var currentScene by remember { mutableStateOf<Scene?>(null) }
    var finished by remember(body) { mutableStateOf(false) }

    val soundPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            soundPlayer.value?.release()
            soundPlayer.value = null
        }
    }

    fun finishOnce() {
        if (!finished) {
            finished = true
            onFinished()
        }
    }

    BackHandler(enabled = !finished) {
        finishOnce()
    }

    fun playSound(url: String, loop: Boolean) {
        soundPlayer.value?.release()
        soundPlayer.value = try {
            MediaPlayer().apply {
                setDataSource(url)
                isLooping = loop
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play cinematic sound: $url", e)
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        SceneComposable(scene = currentScene)

        when (val node = body.getOrNull(index)) {
            is Node.IntroSentence -> IntroSentenceLine(
                node = node,
                onDone = { index++ }
            )

            else -> Unit
        }
    }

    LaunchedEffect(index, body) {
        when (val node = body.getOrNull(index)) {
            null -> if (body.isNotEmpty()) finishOnce()

            is Node.Scene -> {
                currentScene = loadScene(node.sceneId)
                index++
            }

            is Node.Sound -> {
                playSound(node.soundUrl, node.loop)
                index++
            }

            is Node.IntroSentence -> Unit

            else -> index++
        }
    }
}

@Composable
private fun IntroSentenceLine(
    node: Node.IntroSentence,
    onDone: () -> Unit,
) {
    var visible by remember(node.id) { mutableStateOf(false) }

    LaunchedEffect(node.id) {
        visible = false
        if (node.delayMs > 0) delay(node.delayMs)
        visible = true
        delay(CINEMATIC_FADE_MS + node.durationMs)
        visible = false
        delay(CINEMATIC_FADE_MS.toLong())
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        contentAlignment = node.alignment.toComposeAlignment(),
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(CINEMATIC_FADE_MS, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(CINEMATIC_FADE_MS, easing = FastOutSlowInEasing)),
        ) {
            Text(
                text = node.text,
                color = Color.White,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun IntroAlignment.toComposeAlignment(): Alignment = when (this) {
    IntroAlignment.START -> Alignment.CenterStart
    IntroAlignment.END -> Alignment.CenterEnd
    IntroAlignment.TOP -> Alignment.TopCenter
    IntroAlignment.BOTTOM -> Alignment.BottomCenter
    IntroAlignment.CENTER -> Alignment.Center
}
