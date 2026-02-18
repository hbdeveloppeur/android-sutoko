package com.purpletear.aiconversation.domain.repository

import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.messages.Conversation
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoice
import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoiceGroup
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {

    fun clear()

    suspend fun getMessagesStream(
        userId: String?,
        aiCharacterId: Int,
        userToken: String?
    ): Flow<List<Message>>


    fun addMessage(message: Message)
    fun updateMessage(message: Message, update: (Message) -> Message)
    fun mark(messages: List<Message>, state: MessageState)
    fun selectChoice(
        choiceGroup: MessageStoryChoiceGroup,
        choice: MessageStoryChoice
    ): Flow<Result<Unit>>

    suspend fun getSettings(
        userId: String?,
        aiCharacterId: Int,
    ): Flow<Conversation?>

    suspend fun sendMessageMedia(
        userId: String,
        token: String,
        aiCharacterId: Int,
        userName: String?,
        mediaId: Int,
        role: MessageRole,
    ): Flow<Result<Unit>>

    suspend fun restartConversation(
        userId: String,
        aiCharacterId: Int,
    ): Flow<Result<Unit>>

    suspend fun saveForFineTuning(
        userId: String,
    ): Flow<Result<Unit>>
}