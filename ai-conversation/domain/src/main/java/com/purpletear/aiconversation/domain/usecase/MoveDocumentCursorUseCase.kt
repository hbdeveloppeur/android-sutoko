package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.enums.Direction
import com.purpletear.aiconversation.domain.repository.ImageGenerationRepository
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