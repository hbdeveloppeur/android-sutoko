package com.purpletear.ai_conversation.ui.screens.conversation.components.conversation_items_list.items.message_image

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageImage
import com.purpletear.ai_conversation.ui.common.utils.getRemoteAssetsUrl
import com.purpletear.ai_conversation.ui.component.angular.AngularLinesBox2
import com.purpletear.ai_conversation.ui.component.circular_gradient.CircularGradient
import com.purpletear.core.date.DateUtils

@Composable
fun MessageImageNarrationComposable(
    modifier: Modifier = Modifier,
    message: MessageImage,
    isDescriptionLoading: Boolean = false,
    onClick: (url: String) -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Row(
                Modifier.padding(start = 8.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatTimestampToDate(message.timestamp, "HH:mm"),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 0.6.sp
                )
                Text(text = " | ", color = Color.White, style = MaterialTheme.typography.labelSmall)
                Text(
                    modifier = Modifier.padding(bottom = 2.dp),
                    text = "選択する",
                    color = Color.LightGray.copy(0.6f),
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(modifier = Modifier.weight(1f))
            }
            Box(
                modifier
                    .aspectRatio(5 / 3f)
                    .clipToBounds()
                    .padding(vertical = 12.dp)
                    .border(.5.dp, Color.White.copy(.1f), shape)
                    .clip(shape)

            ) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                        ) {
                            onClick(message.url)
                        },
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getRemoteAssetsUrl(message.url))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(.3f))
                )
                CircularGradient()

                AngularLinesBox2(
                    Modifier
                        .align(Alignment.TopEnd)
                        .alpha(0.1f)
                )

                AngularLinesBox2(
                    Modifier
                        .align(Alignment.BottomStart)
                        .scale(-1f, -1f)
                        .alpha(0.1f)
                )
            }
            Row(
                Modifier
                    .padding(end = 8.dp, bottom = 2.dp)
                    .height(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Spacer(modifier = Modifier.weight(1f))
                if (isDescriptionLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp),
                        color = Color.LightGray,
                        strokeWidth = 2.dp
                    )
                } else {
                    message.description?.let { description ->
                        Text(
                            text = description,
                            color = Color.LightGray.copy(0.6f),
                            style = MaterialTheme.typography.labelSmall,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}