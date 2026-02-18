package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoice
import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoiceGroup
import com.purpletear.aiconversation.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SelectMessageChoice @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(
        choiceGroup: MessageStoryChoiceGroup,
        choice: MessageStoryChoice
    ): Flow<Result<Unit>> {
        return conversationRepository.selectChoice(
            choiceGroup,
            choice
        )
    }
}