package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.ExtractZipParams
import com.purpletear.sutoko.game.repository.ZipRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for extracting zip files.
 *
 * @property zipRepository The repository that handles zip file operations.
 */
class ExtractZipUseCase @Inject constructor(
    private val zipRepository: ZipRepository
) {
    /**
     * Extracts a zip file using the provided parameters.
     *
     * @param params The parameters for extraction.
     * @return A Flow that emits a Boolean indicating whether the extraction was successful.
     */
    operator fun invoke(params: ExtractZipParams): Flow<Result<Unit>> {
        return zipRepository.extractZip(params)
    }
}