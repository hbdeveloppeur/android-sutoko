package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.WorkSansFontFamily
import com.purpletear.game.debug.PreviewCharacter
import com.purpletear.game.presentation.common.extensions.toComposeColor
import com.purpletear.game.presentation.common.extensions.toWhitenedComposeColor
import com.purpletear.game.presentation.game_play.components.Avatar
import com.purpletear.sutoko.game.model.character.Character
import com.purpletear.sutoko.game.model.character.CharacterColor

@Preview(name = "GameMessageText")
@Composable
private fun Preview() {
    val gradientCharacter = PreviewCharacter.copy(
        color = CharacterColor(
            startingColor = "#8E2DE2",
            endingColor = "#4A00E0",
        )
    )
    Box(Modifier.padding(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MessageText(
                text = "Super c'est le chapitre 2",
                character = gradientCharacter,
            )
            MessageText(
                text = "Super c'est le chapitre 2, et alors qu'en est-il du chapitre 20?",
                character = gradientCharacter,
            )
        }
    }
}

@Composable
internal fun MessageText(
    modifier: Modifier = Modifier,
    text: String,
    character: Character,
    showHeader: Boolean = true,
    positionInGroup: MessagePositionInGroup = MessagePositionInGroup.SINGLE,
    onAvatarClick: (imageModel: Any?, bounds: Rect) -> Unit = { _, _ -> },
) {
    val alignment = if (character.isMainCharacter) Alignment.CenterEnd else Alignment.CenterStart
    Box(Modifier.fillMaxWidth(), contentAlignment = alignment) {
        if (character.isMainCharacter) {
            MessageMainCharacter(
                modifier = modifier,
                text = text,
                character = character,
                showHeader = showHeader,
                positionInGroup = positionInGroup,
                onAvatarClick = onAvatarClick,
            )
        } else {
            MessageDest(
                modifier = modifier,
                text = text,
                character = character,
                showHeader = showHeader,
                positionInGroup = positionInGroup,
                onAvatarClick = onAvatarClick,
            )
        }
    }
}

@Composable
private fun MessageDest(
    modifier: Modifier = Modifier,
    text: String,
    character: Character? = null,
    showHeader: Boolean = true,
    positionInGroup: MessagePositionInGroup = MessagePositionInGroup.SINGLE,
    onAvatarClick: (imageModel: Any?, bounds: Rect) -> Unit = { _, _ -> },
) {
    val shape = messageBubbleShape(
        isMainCharacter = false,
        positionInGroup = positionInGroup,
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        if (showHeader) character?.let {
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ClickableAvatar(
                    character = it,
                    onAvatarClick = onAvatarClick,
                )
                Name(it.name, color = it.color.toComposeColor())
            }
        }

        MessageBubble(modifier = modifier, shape = shape) {
            Text(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .padding(horizontal = 8.dp),
                text = text,
                color = character?.color?.toWhitenedComposeColor(fraction = 0.5f) ?: Color.White,
                fontFamily = WorkSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun MessageMainCharacter(
    modifier: Modifier = Modifier,
    text: String,
    character: Character? = null,
    showHeader: Boolean = true,
    positionInGroup: MessagePositionInGroup = MessagePositionInGroup.SINGLE,
    onAvatarClick: (imageModel: Any?, bounds: Rect) -> Unit = { _, _ -> },
) {
    val shape = messageBubbleShape(
        isMainCharacter = true,
        positionInGroup = positionInGroup,
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
    ) {
        if (showHeader) character?.let {
            Row(
                modifier = Modifier.padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Name(it.name, color = it.color.toComposeColor())
                ClickableAvatar(
                    character = it,
                    onAvatarClick = onAvatarClick,
                )
            }
        }



        MessageBubble(modifier = modifier, shape = shape) {
            Text(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .padding(horizontal = 8.dp),
                text = text,
                color = character?.color?.toWhitenedComposeColor(fraction = 0.5f) ?: Color.White,
                fontFamily = WorkSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun Name(name: String, color: Color) {
    Text(
        text = name,
        color = color,
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
    )
}

@Composable
private fun ClickableAvatar(
    character: Character,
    onAvatarClick: (imageModel: Any?, bounds: Rect) -> Unit,
) {
    var bounds by remember { mutableStateOf(Rect.Zero) }
    val avatarModel = character.avatar
    val clickableModifier = if (avatarModel != null) {
        Modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onAvatarClick(avatarModel, bounds) }
            )
    } else {
        Modifier
    }

    val avatarColor = character.color.toComposeColor()
    Box(clickableModifier) {
        Avatar(
            modifier = Modifier
                .background(avatarColor, CircleShape),
            size = 22.dp,
            borderWidth = 1.4.dp,
            borderColor = avatarColor,
            imageModel = avatarModel
        )
    }
}

private const val BUBBLE_CORNER_LARGE_DP = 22f
private const val BUBBLE_CORNER_SMALL_DP = 12f

@Composable
private fun messageBubbleShape(
    isMainCharacter: Boolean,
    positionInGroup: MessagePositionInGroup,
): Shape {
    val large = BUBBLE_CORNER_LARGE_DP.dp
    val small = BUBBLE_CORNER_SMALL_DP.dp
    return remember(isMainCharacter, positionInGroup) {
        when (positionInGroup) {
            MessagePositionInGroup.SINGLE -> RoundedCornerShape(large)
            MessagePositionInGroup.TOP -> if (isMainCharacter) {
                RoundedCornerShape(
                    topStart = large,
                    topEnd = large,
                    bottomStart = large,
                    bottomEnd = small
                )
            } else {
                RoundedCornerShape(
                    topStart = large,
                    topEnd = large,
                    bottomStart = small,
                    bottomEnd = large
                )
            }

            MessagePositionInGroup.MIDDLE -> if (isMainCharacter) {
                RoundedCornerShape(
                    topStart = large,
                    topEnd = small,
                    bottomStart = large,
                    bottomEnd = small
                )
            } else {
                RoundedCornerShape(
                    topStart = small,
                    topEnd = large,
                    bottomStart = small,
                    bottomEnd = large
                )
            }

            MessagePositionInGroup.BOTTOM -> if (isMainCharacter) {
                RoundedCornerShape(
                    topStart = large,
                    topEnd = small,
                    bottomStart = large,
                    bottomEnd = large
                )
            } else {
                RoundedCornerShape(
                    topStart = small,
                    topEnd = large,
                    bottomStart = large,
                    bottomEnd = large
                )
            }
        }
    }
}



