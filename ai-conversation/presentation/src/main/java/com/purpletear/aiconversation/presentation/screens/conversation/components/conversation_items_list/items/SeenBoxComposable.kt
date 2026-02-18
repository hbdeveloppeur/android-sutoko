package com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring.StiffnessHigh
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.presentation.R

@Composable
internal fun SeenBoxComposable(
    modifier: Modifier = Modifier,
    state: MessageState = MessageState.Sending,
    isLast: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = CircleShape
    Box(
        modifier = modifier
            .width(12.dp)
            .height(24.dp)
            .clip(shape),
        contentAlignment = Alignment.BottomCenter,
    ) {

        if (state == MessageState.Sent || state == MessageState.Sending) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .border(1.dp, Color.White, shape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier
                        .size(6.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.icons8_check_384),
                    contentDescription = "icon send a message",
                    tint = Color.White.copy(if (state == MessageState.Sending) 0.3f else 1f)
                )
            }
        } else if (state == MessageState.PreSending) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .alpha(0.3f)
                    .border(1.dp, Color.White.copy(0.3f), shape)
            )
        }

        AnimatedVisibility(
            visible = state == MessageState.Seen && isLast, enter = slideInVertically(
                initialOffsetY = { -it * 3 },
                animationSpec = spring(
                    stiffness = StiffnessHigh
                ),
            )
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .border(1.dp, Color.White.copy(0.5f), shape)
            ) {
                content()
            }
        }
    }
}