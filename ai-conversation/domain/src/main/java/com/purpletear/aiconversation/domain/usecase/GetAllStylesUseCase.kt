package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.Style
import com.purpletear.aiconversation.domain.repository.StyleRepository
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