package com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items.vocal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.purpletear.aiconversation.domain.enums.ConversationMode
import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.model.messages.entities.MessageVocal
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.common.utils.getRemoteAssetsUrl
import com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items.SeenBoxComposable
import com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items.message_text.AnimatedWidthComposable


@Composable
internal fun MessageVocalMe(
    modifier: Modifier = Modifier,
    message: MessageVocal,
    character: AiCharacter?,
    mode: ConversationMode,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(Modifier.weight(1f))

        MessageVocalBox()

        AnimatedWidthComposable(
            isDisplay = mode == ConversationMode.Sms,
            maxWidth = 12.dp
        ) {
            SeenBoxComposable(
                state = message.state
            ) {
                Avatar(
                    url = getRemoteAssetsUrl(character?.avatarUrl ?: ""),
                    size = 12.dp
                )
            }
        }
    }
}


@Composable
private fun Avatar(modifier: Modifier = Modifier, url: String, size: Dp = 24.dp) {
    AsyncImage(
        model = url, contentDescription = "Message avatar", modifier = modifier
            .size(size)
            .clip(CircleShape)
    )
}


@Composable
fun MessageVocalBox(modifier: Modifier = Modifier) {
    val linearGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF4327E2), Color(0xFF6E33B1))
    )
    Box(
        modifier = modifier
            .widthIn(max = 200.dp)
            .clip(RoundedCornerShape(7.dp, 7.dp, 7.dp, 7.dp))
            .background(linearGradient)
            .padding(PaddingValues(horizontal = 16.dp, vertical = 10.dp))
            .widthIn(max = 200.dp),
        content = {
            Image(
                modifier = Modifier.height(20.dp),
                painter = painterResource(id = R.drawable.ic_sound_wave),
                contentDescription = null
            )
        }
    )
}