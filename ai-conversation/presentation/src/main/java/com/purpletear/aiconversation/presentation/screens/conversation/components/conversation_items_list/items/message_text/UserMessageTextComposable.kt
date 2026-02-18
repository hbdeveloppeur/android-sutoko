package com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items.message_text

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.purpletear.aiconversation.domain.enums.ConversationMode
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.model.messages.entities.MessageText
import com.purpletear.aiconversation.presentation.common.utils.getRemoteAssetsUrl
import com.purpletear.aiconversation.presentation.component.blurred_message.MessagePositionInGroup
import com.purpletear.aiconversation.presentation.component.clipped_text.ClippedTextComposable
import com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items.SeenBoxComposable


@Composable
internal fun UserMessageTextComposable(
    modifier: Modifier = Modifier,
    message: MessageText,
    character: AiCharacter?,
    groupPosition: MessagePositionInGroup,
    mode: ConversationMode,
) {
    PrivateUserMessageTextComposable(
        text = message.text,
        state = message.state,
        character = character,
        groupPosition = groupPosition,
        mode = mode,
        timestamp = message.timestamp,
    )
}

@Composable
internal fun PrivateUserMessageTextComposable(
    modifier: Modifier = Modifier,
    text: String,
    character: AiCharacter?,
    state: MessageState = MessageState.Sending,
    groupPosition: MessagePositionInGroup,
    mode: ConversationMode,
    timestamp: Long,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        Spacer(Modifier.weight(1f))
        MessageBox(modifier = Modifier.padding(start = 8.dp), groupPosition = groupPosition) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MessageText(text = text)
            }
        }

        AnimatedWidthComposable(
            isDisplay = mode == ConversationMode.Sms,
            maxWidth = 2.dp
        ) {
            Spacer(modifier = Modifier.width(2.dp))
        }

        AnimatedWidthComposable(
            isDisplay = mode == ConversationMode.Sms,
            maxWidth = 12.dp
        ) {
            SeenBoxComposable(
                state = state
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
fun AnimatedWidthComposable(
    isDisplay: Boolean,
    maxWidth: Dp,
    content: @Composable() (BoxScope.() -> Unit)
) {
    // Determine target width based on the isDisplay flag
    val targetWidth = if (isDisplay) maxWidth else 0.dp

    // Animate the width transition
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = tween(durationMillis = 280),
        label = "Animation of the avatar" // Customize duration as needed
    )

    // Box with animated width
    Box(
        modifier = Modifier
            .width(animatedWidth), content = content
    )
}

@Composable
private fun MessageText(text: String) {
    ClippedTextComposable(text)
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
private fun MessageBox(
    modifier: Modifier = Modifier,
    groupPosition: MessagePositionInGroup,
    content: @Composable BoxScope.() -> Unit
) {
    val small = 12.dp
    val medium = 14.dp
    val large = 16.dp

    val topEndCorner = when (groupPosition) {
        MessagePositionInGroup.PositionFirst -> large
        MessagePositionInGroup.PositionMiddle, MessagePositionInGroup.PositionSingle -> medium
        MessagePositionInGroup.PositionLast -> small
    }

    val bottomEndCorner = when (groupPosition) {
        MessagePositionInGroup.PositionFirst -> small
        MessagePositionInGroup.PositionMiddle, MessagePositionInGroup.PositionSingle -> medium
        MessagePositionInGroup.PositionLast -> large
    }

    val shape = RoundedCornerShape(
        topStart = large,
        topEnd = topEndCorner,
        bottomStart = large,
        bottomEnd = bottomEndCorner
    )

    val color = Color(0xFF101014)
    Box(
        modifier = modifier
            .heightIn(min = 36.dp)
            .widthIn(min = 42.dp)
            .background(color, shape = shape)
            .border(.5.dp, Color.White.copy(0.15f), shape)
            .padding(bottom = 8.dp, top = 8.dp, start = 10.dp, end = 12.dp)
            .padding(start = 4.dp),
        content = content,
        contentAlignment = Alignment.Center
    )
}