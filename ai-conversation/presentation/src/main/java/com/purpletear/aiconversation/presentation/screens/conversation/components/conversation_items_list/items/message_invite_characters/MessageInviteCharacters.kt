package com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items.message_invite_characters

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.purpletear.aiconversation.domain.enums.Visibility
import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.common.utils.getRemoteAssetsUrl
import com.purpletear.aiconversation.presentation.theme.AiConversationTheme


@Composable
@Preview(name = "MessageInviteCharacters", showBackground = false, showSystemUi = false)
private fun Preview() {

    val character = AiCharacter(
        id = 1,
        firstName = "Eva",
        lastName = "",
        description = "",
        avatarUrl = "https://data.sutoko.app/resources/sutoko-ai/image/avatar_hanna.jpg",
        bannerUrl = "",
        createdAt = System.currentTimeMillis(),
        visibility = Visibility.Private,
        statusDescription = "",
        code = "",
    )

    val verticalRules = listOf(14.dp)
    val rulesEnabled = false
    AiConversationTheme {
        Box {
            Column(
                Modifier.background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.preview_tool_button),
                    contentDescription = null,
                )
                Box(Modifier.padding(vertical = 12.dp)) {
                    MessageInviteCharactersComposable(
                        modifier = Modifier
                            .fillMaxWidth(0.9f),
                        characters = listOf(
                            character,
                            character,
                        )
                    )
                }
            }
            if (rulesEnabled) {
                verticalRules.forEach { startPadding ->
                    Box(
                        Modifier
                            .padding(start = startPadding)
                            .fillMaxHeight()
                            .width(1.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun MessageInviteCharactersComposable(
    modifier: Modifier = Modifier,
    characters: List<AiCharacter>
) {
    val avatarSize = 22.dp
    Row(modifier = modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .height(avatarSize)
                .width(((characters.size - 1) * 14).dp + avatarSize)
        ) {
            characters.forEachIndexed { index, item ->
                item.avatarUrl?.let { avatarUrl ->
                    Avatar(
                        modifier = Modifier
                            .size(avatarSize)
                            .offset(x = (index * 14).dp)
                            .shadow(4.dp, CircleShape),
                        url = getRemoteAssetsUrl(avatarUrl)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        val strCharacter = characters.joinToString(", ") { it.firstName }
        Text(
            modifier = Modifier.widthIn(max = 300.dp),
            text = when (characters.size) {
                1 -> stringResource(
                    R.string.ai_conversation_message_invite_characters_singular,
                    strCharacter
                )

                else -> stringResource(
                    R.string.ai_conversation_message_invite_characters_plural,
                    strCharacter
                )
            },
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun Avatar(modifier: Modifier = Modifier, url: String) {
    AsyncImage(
        model = url, contentDescription = "Message avatar", modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .border(2.dp, Color(0xFF8D94AA), CircleShape)
    )
}