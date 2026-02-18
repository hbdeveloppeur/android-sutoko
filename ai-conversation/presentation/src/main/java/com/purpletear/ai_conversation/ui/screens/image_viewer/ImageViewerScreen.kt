package com.purpletear.ai_conversation.ui.screens.image_viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.ai_conversation.presentation.R
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun ImageViewerScreen(url: String) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        val (image, indication) = createRefs()

        Text(
            modifier = Modifier.constrainAs(indication) {
                bottom.linkTo(image.top, margin = 12.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            text = stringResource(R.string.ai_conversation_image_viewer_gesture_indication),
            style = MaterialTheme.typography.labelSmall,
            fontStyle = FontStyle.Italic,
            color = Color.White.copy(0.5f)
        )
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .zoomable(rememberZoomState()),
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )


    }
}