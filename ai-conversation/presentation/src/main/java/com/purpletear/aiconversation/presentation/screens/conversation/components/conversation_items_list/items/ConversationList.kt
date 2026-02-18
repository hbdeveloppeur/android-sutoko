package com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.purpletear.aiconversation.domain.enums.ConversationMode
import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.model.messages.entities.MessageImage
import com.purpletear.aiconversation.domain.model.messages.entities.MessageInviteCharacters
import com.purpletear.aiconversation.domain.model.messages.entities.MessageNarration
import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoiceGroup
import com.purpletear.aiconversation.domain.model.messages.entities.MessageText
import com.purpletear.aiconversation.domain.model.messages.entities.MessageVocal
import com.purpletear.aiconversation.presentation.common.utils.getRemoteAssetsUrl
import com.purpletear.aiconversation.presentation.component.blurred_message.MessagePositionInGroup
import com.purpletear.aiconversation.presentation.model.UIMessage
import com.purpletear.aiconversation.presentation.navigation.AiConversationRouteDestination
import com.purpletear.aiconversation.presentation.screens.conversation.components.alert.AlertComposable
import com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items.choice.ChoiceComposable
import com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items.message_image.MessageImageNarrationComposable
import com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items.message_invite_characters.MessageInviteCharactersComposable
import com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items.message_text.MessageComposable
import com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items.message_text.UserMessageTextComposable
import com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items.vocal.MessageVocalMe
import com.purpletear.aiconversation.presentation.screens.conversation.viewmodels.ConversationViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@Composable
internal fun ConversationList(
    modifier: Modifier,
    state: LazyListState,
    viewModel: ConversationViewModel,
    navController: NavController,
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 32.dp, start = 12.dp, end = 12.dp),
        reverseLayout = true
    ) {
        viewModel.alert.value?.let { alert ->
            item(
                key = "alert"
            ) {
                AlertComposable(state = alert, onClick = viewModel::onAlertClick)
            }
        }

        items(viewModel.messages, viewModel, navController)

    }
}

private fun LazyListScope.chatItemsIndexed(
    items: List<UIMessage>,
    key: (index: Int, item: UIMessage) -> String,
    content: @Composable (index: Int, item: UIMessage) -> Unit, // Remove LazyListScope from here
) {
    itemsIndexed(items, key = key) { index, item ->
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            if (listOf(
                    MessagePositionInGroup.PositionFirst,
                    MessagePositionInGroup.PositionSingle
                ).contains(item.shape)
            ) {
                Spacer(modifier = Modifier.size(8.dp))
            }
            content(index, item)
            if (listOf(
                    MessagePositionInGroup.PositionLast,
                    MessagePositionInGroup.PositionSingle
                ).contains(item.shape)
            ) {
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }
}

private fun LazyListScope.items(
    items: List<UIMessage>,
    viewModel: ConversationViewModel,
    navController: NavController,
) {
    chatItemsIndexed(items, key = { _, item ->
        "${item.message.timestamp}_${item.message.id}"
    }) { index, item ->
        
        when (item.message) {
            is MessageText -> {
                when (item.message.role) {
                    MessageRole.Assistant -> {
                        MessageComposable(
                            modifier = Modifier,
                            message = item.message,
                            character = viewModel.characters[item.message.aiCharacterId]
                                ?: viewModel.conversationSettings.value?.character,
                            groupPosition = item.shape,
                            displaysDate = item.displaysDate
                        )
                    }

                    MessageRole.Narrator -> {
                        ConversationNarrationComposable(text = item.message.text)
                    }

                    else -> {
                        UserMessageTextComposable(
                            message = item.message,
                            character = viewModel.conversationSettings.value?.character,
                            groupPosition = item.shape,
                            mode = viewModel.conversationSettings.value?.mode
                                ?: ConversationMode.Sms
                        )
                    }
                }
            }

            is MessageStoryChoiceGroup -> {
                ChoiceComposable(
                    modifier = Modifier,
                    messageStoryChoiceGroup = item.message,
                    viewModel = viewModel,
                )
            }

            is MessageImage -> {
                when (item.message.role) {
                    MessageRole.Narrator -> {
                        MessageImageNarrationComposable(
                            modifier = Modifier
                                .fillMaxWidth(1f),
                            isDescriptionLoading = viewModel.loadingDescriptionMessageId.value == item.message.id,
                            message = item.message,
                            onClick = { url ->
                                val encodedUrl = URLEncoder.encode(
                                    getRemoteAssetsUrl(url = url),
                                    StandardCharsets.UTF_8.toString()
                                )
                                navController.navigate(
                                    AiConversationRouteDestination.ImageViewer(
                                        url = encodedUrl
                                    ).destination
                                )
                            },

                            )
                    }

                    else -> {

                    }
                }
            }

            is MessageInviteCharacters -> {
                MessageInviteCharactersComposable(
                    modifier = Modifier,
                    characters = item.message.characters
                )
            }

            is MessageVocal -> {
                MessageVocalMe(
                    message = item.message,
                    mode = viewModel.conversationSettings.value?.mode ?: ConversationMode.Sms,
                    character = viewModel.conversationSettings.value?.character
                )
            }

            is MessageNarration -> {
                ConversationNarrationComposable(text = item.message.text)
            }
        }

    }
}