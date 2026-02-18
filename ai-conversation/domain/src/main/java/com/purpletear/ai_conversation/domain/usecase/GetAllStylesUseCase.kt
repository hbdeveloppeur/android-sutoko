package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.Style
import com.purpletear.ai_conversation.domain.repository.StyleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllStylesUseCase @Inject constructor(
    private val styleRepository: StyleRepository
) {
    suspend operator fun invoke(
    ): Flow<Result<List<Style>>> {
        return styleRepository.getAll()
    }
}