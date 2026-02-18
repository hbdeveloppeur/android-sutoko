package com.purpletear.ai_conversation.ui.screens.conversation.components.conversation_items_list.items.message_text

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.purpletear.ai_conversation.domain.model.AiCharacter
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageText
import com.purpletear.ai_conversation.ui.common.utils.getRemoteAssetsUrl
import com.purpletear.ai_conversation.ui.component.blurred_message.MessagePositionInGroup
import com.purpletear.ai_conversation.ui.component.clipped_text.ClippedTextComposable
import com.purpletear.core.date.DateUtils


@Composable
internal fun MessageComposable(
    modifier: Modifier = Modifier,
    message: MessageText,
    character: AiCharacter?,
    groupPosition: MessagePositionInGroup,
    displaysDate: Boolean,
) {
    PrivateMessageComposable(
        modifier = modifier,
        text = message.text,
        groupPosition = groupPosition,
        timestamp = message.timestamp,
        character = character,
        displaysDate = displaysDate,
    )
}

@Composable
private fun PrivateMessageComposable(
    modifier: Modifier = Modifier,
    text: String,
    groupPosition: MessagePositionInGroup,
    timestamp: Long,
    character: AiCharacter?,
    displaysDate: Boolean,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MessageBox(groupPosition = groupPosition) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Avatar(url = getRemoteAssetsUrl(character?.avatarUrl ?: ""))
                MessageText(text = text)
            }
        }
        if (displaysDate) {
            Text(
                text = DateUtils.formatTimestampToDate(timestamp, "HH:mm"),
                color = Color.White.copy(0.5f),
                fontSize = 10.sp,
                letterSpacing = 0.1.sp
            )
        }
    }
}

@Composable
private fun MessageText(text: String) {

    ClippedTextComposable(modifier = Modifier, message = text)
}

@Composable
private fun Avatar(modifier: Modifier = Modifier, url: String) {
    AsyncImage(
        model = url, contentDescription = "Message avatar", modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(1.dp, Color.White, CircleShape)
    )
}

@Composable
private fun MessageBox(
    modifier: Modifier = Modifier,
    groupPosition: MessagePositionInGroup,
    content: @Composable BoxScope.() -> Unit
) {
    val small = 12.dp
    val medium = 14.dp
    val large = 16.dp

    val topStartCorner = when (groupPosition) {
        MessagePositionInGroup.PositionFirst -> large
        MessagePositionInGroup.PositionMiddle, MessagePositionInGroup.PositionSingle -> medium
        MessagePositionInGroup.PositionLast -> small
    }

    val bottomStartCorner = when (groupPosition) {
        MessagePositionInGroup.PositionFirst -> small
        MessagePositionInGroup.PositionMiddle, MessagePositionInGroup.PositionSingle -> medium
        MessagePositionInGroup.PositionLast -> large
    }

    val shape = RoundedCornerShape(
        topStart = topStartCorner,
        topEnd = large,
        bottomStart = bottomStartCorner,
        bottomEnd = large
    )

    val color = Color(0xFF212637)
    Box(
        modifier = Modifier
            .background(color, shape = shape)
            .border(.5.dp, Color.White.copy(0.15f), shape)
            .padding(bottom = 6.dp, top = 6.dp, start = 8.dp, end = 6.dp)
            .padding(end = 10.dp),
        content = content,
    )
}