package fr.purpletear.sutoko.screens.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.core.presentation.components.AnimatedNewsGradient
import com.purpletear.version_presentation.R
import com.purpletear.version_presentation.components.announce_card.GameAnnounceCard

@Composable
internal fun CreatePageComposable(modifier: Modifier = Modifier) {
    val (visible, setVisible) = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { setVisible(true) }


    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {


        Box {
            AsyncImage(
                modifier = Modifier.matchParentSize(),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.tmp_announce)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.95f))
            )
        }

        AnimatedNewsGradient(Modifier.fillMaxSize(), alpha = 0.02f)
        AnnounceCard(visible)

    }
}


@Composable
private fun AnnounceCard(visible: Boolean) {

    val durationMillis = 560
    val easing = FastOutSlowInEasing
    ConstraintLayout(Modifier.fillMaxSize()) {
        val (card) = createRefs()

        AnimatedVisibility(
            modifier = Modifier.constrainAs(card) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                verticalBias = 0.4f
            },
            visible = visible,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = easing
                )
            ) +
                    slideInVertically(
                        animationSpec = tween(durationMillis = durationMillis, easing = easing),
                        initialOffsetY = { fullHeight -> fullHeight / 8 }
                    ),
        ) {
            GameAnnounceCard(Modifier)
        }
    }
}