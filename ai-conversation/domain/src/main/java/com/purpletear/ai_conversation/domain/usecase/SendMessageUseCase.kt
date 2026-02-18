package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageText
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageVocal
import com.purpletear.ai_conversation.domain.repository.MessageRepository
import com.purpletear.ai_conversation.domain.repository.WebSocketDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val webSocketDataSource: WebSocketDataSource,
) {
    suspend operator fun invoke(
        userId: String,
        token: String,
        userName: String?,
        characterId: Int,
        messages: List<Message>,
    ): Flow<Result<Unit>> {

        if (messages.any { it is MessageVocal }) {
            return messageRepository.sendMessage(
                uid = userId,
                token = token,
                characterId = characterId,
                texts = messages.filterIsInstance<MessageText>().map { it.text },
                userName = userName,
                audioFiles = messages.filterIsInstance<MessageVocal>()
                    .filter { it.file != null }.map { it.file!! },
            )
        }

        return webSocketDataSource.sendMessage(
            characterId = characterId,
            messages = messages,
            userId = userId,
            token = token,
            userName = userName,
        )
    }
}