package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.enums.Direction
import com.purpletear.ai_conversation.domain.repository.ImageGenerationRepository
import javax.inject.Inject

class MoveDocumentCursorUseCase @Inject constructor(
    private val imageGenerationRepository: ImageGenerationRepository,
) {
    operator fun invoke(
        direction: Direction,
    ) {
        return imageGenerationRepository.moveDocumentCursor(direction = direction)
    }
}