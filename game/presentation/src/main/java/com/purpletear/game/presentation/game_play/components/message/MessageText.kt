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
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.MontserratFontFamily
import com.purpletear.game.debug.PreviewCharacter
import com.purpletear.game.presentation.game_play.components.Avatar
import com.purpletear.sutoko.game.model.character.Character

@Preview(name = "GameMessageText")
@Composable
private fun Preview() {
    Box(Modifier.padding(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MessageText(
                text = "Super c'est le chapitre 2",
                character = PreviewCharacter,
            )
            MessageText(
                text = "Super c'est le chapitre 2, et alors qu'en est-il du chapitre 20?",
                character = PreviewCharacter,
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
                onAvatarClick = onAvatarClick,
            )
        } else {
            MessageDest(
                modifier = modifier,
                text = text,
                character = character,
                showHeader = showHeader,
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
    onAvatarClick: (imageModel: Any?, bounds: Rect) -> Unit = { _, _ -> },
) {
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
                Name(it.name)
            }
        }



        MessageBubble(modifier = modifier) {
            Text(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .padding(horizontal = 8.dp),
                text = text,
                color = Color.White,
                fontFamily = MontserratFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.7.sp,
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
    onAvatarClick: (imageModel: Any?, bounds: Rect) -> Unit = { _, _ -> },
) {
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
                Name(it.name)
                ClickableAvatar(
                    character = it,
                    onAvatarClick = onAvatarClick,
                )
            }
        }



        MessageBubble(modifier = modifier) {
            Text(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .padding(horizontal = 8.dp),
                text = text,
                color = Color.White,
                fontFamily = MontserratFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.7.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun Name(name: String) {
    Text(
        text = name,
        color = Color.White,
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
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

    Box(clickableModifier) {
        Avatar(
            modifier = Modifier.background(character.avatarColor()),
            size = 22.dp,
            borderWidth = 1.4.dp,
            imageModel = avatarModel
        )
    }
}

private fun Character.avatarColor(): Color {
    return color.startingColor.toComposeColor() ?: Color.Blue
}

private fun String.toComposeColor(): Color? = try {
    Color(android.graphics.Color.parseColor(this))
} catch (_: IllegalArgumentException) {
    null
}

