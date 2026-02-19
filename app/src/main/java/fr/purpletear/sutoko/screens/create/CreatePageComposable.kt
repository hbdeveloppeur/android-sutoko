package fr.purpletear.sutoko.screens.create

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.core.presentation.components.AnimatedNewsGradient
import com.purpletear.version.presentation.R
import com.purpletear.version.presentation.components.announcement.YourTurnAnnouncementCard

@Composable
internal fun CreatePageComposable(modifier: Modifier = Modifier) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

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
        YourTurnAnnouncementCard(
            onDismiss = { backDispatcher?.onBackPressed() }
        )
    }
}