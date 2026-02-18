package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.ExtractZipParams
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for zip file operations.
 */
interface ZipRepository {
    /**
     * Extracts a zip file to the specified destination.
     *
     * @param params The parameters for extraction.
     * @return A Flow that emits a Boolean indicating whether the extraction was successful.
     */
    fun extractZip(params: ExtractZipParams): Flow<Result<Unit>>
}