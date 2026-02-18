package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.messages.entities.MessageStoryChoice
import com.purpletear.ai_conversation.domain.repository.StoryChoiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MakeStoryChoiceUseCase @Inject constructor(
    private val storyChoiceRepository: StoryChoiceRepository,
) {
    suspend operator fun invoke(
        userId: String?,
        choice: MessageStoryChoice,
    ): Flow<Result<Unit>> {
        return storyChoiceRepository.makeChoice(
            userId = userId,
            choice = choice,
        )
    }
}