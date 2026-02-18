package com.purpletear.aiconversation.domain.repository

import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoice
import kotlinx.coroutines.flow.Flow

interface StoryChoiceRepository {
    suspend fun makeChoice(
        userId: String?,
        choice: MessageStoryChoice
    ): Flow<Result<Unit>>

}