package com.purpletear.ai_conversation.domain.repository

import com.purpletear.ai_conversation.domain.model.messages.entities.MessageStoryChoice
import kotlinx.coroutines.flow.Flow

interface StoryChoiceRepository {
    suspend fun makeChoice(
        userId: String?,
        choice: MessageStoryChoice
    ): Flow<Result<Unit>>

}